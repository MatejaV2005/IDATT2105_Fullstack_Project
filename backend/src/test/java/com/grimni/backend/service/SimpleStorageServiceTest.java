package com.grimni.backend.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.Course;
import com.grimni.domain.FileCourseBridge;
import com.grimni.domain.FileObject;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.AccessLevel;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.repository.CourseRepository;
import com.grimni.repository.FileCourseBridgeRepository;
import com.grimni.repository.FileObjectRepository;
import com.grimni.service.SimpleStorageService;

import jakarta.persistence.EntityNotFoundException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
public class SimpleStorageServiceTest {

    private static final String BUCKET = "test-bucket";

    @Mock private S3Client s3Client;
    @Mock private FileObjectRepository repository;
    @Mock private CourseRepository courseRepository;
    @Mock private FileCourseBridgeRepository fileCourseBridgeRepository;

    private SimpleStorageService service;

    private Organization org;
    private Organization otherOrg;
    private User uploader;

    @BeforeEach
    void setUp() {
        service = new SimpleStorageService(s3Client, repository, courseRepository, fileCourseBridgeRepository, BUCKET);

        org = new Organization();
        ReflectionTestUtils.setField(org, "id", 10L);

        otherOrg = new Organization();
        ReflectionTestUtils.setField(otherOrg, "id", 20L);

        uploader = new User();
        ReflectionTestUtils.setField(uploader, "id", 1L);
        uploader.setLegalName("alice");
    }

    private OrgUserBridge membership(Organization o, OrgUserRole role) {
        OrgUserBridge bridge = new OrgUserBridge();
        bridge.setOrganization(o);
        bridge.setUserRole(role);
        return bridge;
    }

    private FileObject fileObject(Long id, AccessLevel read, AccessLevel delete, Organization owner, String key) {
        FileObject f = new FileObject();
        ReflectionTestUtils.setField(f, "id", id);
        f.setReadAccess(read);
        f.setDeleteAccess(delete);
        f.setOrganization(owner);
        f.setObjectKey(key);
        f.setFileName("doc.pdf");
        return f;
    }

    @Nested
    @DisplayName("upload")
    class UploadTests {

        @Test
        @DisplayName("uploads to S3 with correct bucket/key and persists metadata")
        void upload_success() {
            InputStream stream = new ByteArrayInputStream("data".getBytes());
            when(repository.save(any(FileObject.class))).thenAnswer(inv -> {
                FileObject f = inv.getArgument(0);
                ReflectionTestUtils.setField(f, "id", 50L);
                return f;
            });

            FileObject result = service.upload(
                    "courses/100/key.pdf", stream, 4L, "application/pdf",
                    AccessLevel.ANYONE_IN_ORG, AccessLevel.MANAGER, uploader, "doc.pdf", org
            );

            assertEquals(50L, result.getId());
            assertEquals("courses/100/key.pdf", result.getObjectKey());
            assertEquals(AccessLevel.ANYONE_IN_ORG, result.getReadAccess());
            assertEquals(AccessLevel.MANAGER, result.getDeleteAccess());
            assertSame(org, result.getOrganization());
            assertSame(uploader, result.getUploadedBy());

            ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(putCaptor.capture(), any(software.amazon.awssdk.core.sync.RequestBody.class));
            assertEquals(BUCKET, putCaptor.getValue().bucket());
            assertEquals("courses/100/key.pdf", putCaptor.getValue().key());
            assertEquals("application/pdf", putCaptor.getValue().contentType());
        }

        @Test
        @DisplayName("compensates by deleting from S3 when DB persistence fails")
        void upload_dbFailure_deletesFromS3() {
            InputStream stream = new ByteArrayInputStream("data".getBytes());
            when(repository.save(any(FileObject.class))).thenThrow(new RuntimeException("db down"));

            assertThrows(RuntimeException.class, () ->
                service.upload(
                    "courses/100/key.pdf", stream, 4L, "application/pdf",
                    AccessLevel.PUBLIC, AccessLevel.OWNER, uploader, "doc.pdf", org
                )
            );

            ArgumentCaptor<DeleteObjectRequest> delCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
            verify(s3Client).deleteObject(delCaptor.capture());
            assertEquals(BUCKET, delCaptor.getValue().bucket());
            assertEquals("courses/100/key.pdf", delCaptor.getValue().key());
        }
    }

    @Nested
    @DisplayName("read")
    class ReadTests {

        @Test
        @DisplayName("returns file bytes with resolved content type")
        void read_success() {
            FileObject f = fileObject(50L, AccessLevel.ANYONE_IN_ORG, AccessLevel.MANAGER, org, "courses/100/key.pdf");
            when(repository.findById(50L)).thenReturn(Optional.of(f));

            byte[] bytes = "hello".getBytes();
            GetObjectResponse response = GetObjectResponse.builder().contentType("application/pdf").build();
            ResponseBytes<GetObjectResponse> respBytes = ResponseBytes.fromByteArray(response, bytes);
            when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(respBytes);

            SimpleStorageService.StoredFile result =
                    service.read(50L, List.of(membership(org, OrgUserRole.WORKER)));

            assertSame(f, result.fileObject());
            assertArrayEquals(bytes, result.bytes());
            assertEquals("application/pdf", result.contentType());
        }

        @Test
        @DisplayName("falls back to application/octet-stream when S3 returns no content type")
        void read_missingContentType_fallsBack() {
            FileObject f = fileObject(50L, AccessLevel.PUBLIC, AccessLevel.OWNER, org, "k");
            when(repository.findById(50L)).thenReturn(Optional.of(f));

            GetObjectResponse response = GetObjectResponse.builder().build();
            ResponseBytes<GetObjectResponse> respBytes = ResponseBytes.fromByteArray(response, new byte[0]);
            when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(respBytes);

            SimpleStorageService.StoredFile result = service.read(50L, null);

            assertEquals("application/octet-stream", result.contentType());
        }

        @Test
        @DisplayName("throws EntityNotFoundException when file metadata missing")
        void read_notFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.read(999L, List.of()));
        }

        @Test
        @DisplayName("throws SecurityException when user is outside org and access is not PUBLIC")
        void read_outsideOrg_denied() {
            FileObject f = fileObject(50L, AccessLevel.ANYONE_IN_ORG, AccessLevel.MANAGER, org, "k");
            when(repository.findById(50L)).thenReturn(Optional.of(f));

            assertThrows(SecurityException.class, () ->
                service.read(50L, List.of(membership(otherOrg, OrgUserRole.OWNER)))
            );
        }

        @Test
        @DisplayName("MANAGER role cannot read OWNER-restricted files")
        void read_managerCannotReadOwnerOnly() {
            FileObject f = fileObject(50L, AccessLevel.OWNER, AccessLevel.OWNER, org, "k");
            when(repository.findById(50L)).thenReturn(Optional.of(f));

            assertThrows(SecurityException.class, () ->
                service.read(50L, List.of(membership(org, OrgUserRole.MANAGER)))
            );
        }

        @Test
        @DisplayName("OWNER can read OWNER-restricted file")
        void read_ownerCanReadOwnerOnly() {
            FileObject f = fileObject(50L, AccessLevel.OWNER, AccessLevel.OWNER, org, "k");
            when(repository.findById(50L)).thenReturn(Optional.of(f));

            GetObjectResponse response = GetObjectResponse.builder().contentType("text/plain").build();
            ResponseBytes<GetObjectResponse> respBytes = ResponseBytes.fromByteArray(response, "ok".getBytes());
            when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(respBytes);

            SimpleStorageService.StoredFile result =
                    service.read(50L, List.of(membership(org, OrgUserRole.OWNER)));

            assertEquals("text/plain", result.contentType());
        }

        @Test
        @DisplayName("PUBLIC files are readable by anonymous users")
        void read_publicAccessibleByAnonymous() {
            FileObject f = fileObject(50L, AccessLevel.PUBLIC, AccessLevel.OWNER, org, "k");
            when(repository.findById(50L)).thenReturn(Optional.of(f));

            GetObjectResponse response = GetObjectResponse.builder().contentType("text/plain").build();
            ResponseBytes<GetObjectResponse> respBytes = ResponseBytes.fromByteArray(response, "ok".getBytes());
            when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(respBytes);

            SimpleStorageService.StoredFile result = service.read(50L, null);
            assertEquals("text/plain", result.contentType());
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("deletes from DB and S3 when caller has access")
        void delete_success() {
            FileObject f = fileObject(50L, AccessLevel.ANYONE_IN_ORG, AccessLevel.MANAGER, org, "courses/100/key.pdf");
            when(repository.findById(50L)).thenReturn(Optional.of(f));

            service.delete(50L, List.of(membership(org, OrgUserRole.MANAGER)));

            verify(repository).delete(f);
            verify(repository).flush();

            ArgumentCaptor<DeleteObjectRequest> delCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
            verify(s3Client).deleteObject(delCaptor.capture());
            assertEquals(BUCKET, delCaptor.getValue().bucket());
            assertEquals("courses/100/key.pdf", delCaptor.getValue().key());
        }

        @Test
        @DisplayName("throws SecurityException and does not delete when user lacks access")
        void delete_denied() {
            FileObject f = fileObject(50L, AccessLevel.ANYONE_IN_ORG, AccessLevel.OWNER, org, "k");
            when(repository.findById(50L)).thenReturn(Optional.of(f));

            assertThrows(SecurityException.class, () ->
                service.delete(50L, List.of(membership(org, OrgUserRole.MANAGER)))
            );

            verify(repository, never()).delete(any(FileObject.class));
            verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("throws EntityNotFoundException when the file is missing")
        void delete_missing() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.delete(999L, List.of(membership(org, OrgUserRole.OWNER))));
        }
    }

    @Nested
    @DisplayName("linkFileToCourse")
    class LinkFileToCourseTests {

        @Test
        @DisplayName("creates a FileCourseBridge tying file to course")
        void linkFileToCourse_success() {
            FileObject f = fileObject(50L, AccessLevel.ANYONE_IN_ORG, AccessLevel.MANAGER, org, "k");
            Course course = new Course();
            course.setId(100L);
            course.setOrganization(org);
            when(repository.findById(50L)).thenReturn(Optional.of(f));
            when(courseRepository.findById(100L)).thenReturn(Optional.of(course));

            service.linkFileToCourse(50L, 100L);

            ArgumentCaptor<FileCourseBridge> bridgeCaptor = ArgumentCaptor.forClass(FileCourseBridge.class);
            verify(fileCourseBridgeRepository).save(bridgeCaptor.capture());
            FileCourseBridge bridge = bridgeCaptor.getValue();
            assertSame(course, bridge.getCourse());
            assertSame(f, bridge.getFile());
        }

        @Test
        @DisplayName("throws when file does not exist")
        void linkFileToCourse_fileMissing() {
            when(repository.findById(50L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.linkFileToCourse(50L, 100L));

            verify(fileCourseBridgeRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when course does not exist")
        void linkFileToCourse_courseMissing() {
            FileObject f = fileObject(50L, AccessLevel.ANYONE_IN_ORG, AccessLevel.MANAGER, org, "k");
            when(repository.findById(50L)).thenReturn(Optional.of(f));
            when(courseRepository.findById(100L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.linkFileToCourse(50L, 100L));

            verify(fileCourseBridgeRepository, never()).save(any());
        }
    }
}
