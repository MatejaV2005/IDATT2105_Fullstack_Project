package com.grimni.backend.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.Certificate;
import com.grimni.domain.Course;
import com.grimni.domain.FileObject;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.dto.CreateCertificateRequest;
import com.grimni.dto.UpdateCertificateRequest;
import com.grimni.repository.CertificateRepository;
import com.grimni.repository.CourseRepository;
import com.grimni.repository.FileObjectRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;
import com.grimni.service.CertificateService;

import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CertificateServiceTest {

    @Mock private CertificateRepository certificateRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileObjectRepository fileObjectRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private OrgUserBridgeRepository orgUserBridgeRepository;

    @InjectMocks
    private CertificateService certificateService;

    private Organization testOrg;
    private User testUser;
    private User targetUser;
    private FileObject testFile;
    private Course testCourse;
    private Certificate testCert;

    @BeforeEach
    void setUp() {
        testOrg = new Organization();
        ReflectionTestUtils.setField(testOrg, "id", 10L);
        testOrg.setOrgName("Test Org");

        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        testUser.setLegalName("alice");

        targetUser = new User();
        ReflectionTestUtils.setField(targetUser, "id", 2L);
        targetUser.setLegalName("bob");

        testFile = new FileObject();
        ReflectionTestUtils.setField(testFile, "id", 50L);

        testCourse = new Course();
        testCourse.setId(100L);
        testCourse.setTitle("Safety Course");

        testCert = new Certificate();
        testCert.setId(200L);
        testCert.setCertificateName("Safety Cert");
        testCert.setUser(targetUser);
        testCert.setFile(testFile);
        testCert.setOrganization(testOrg);
        testCert.setCourse(testCourse);
    }

    private void stubOrgMembership(Long orgId, Long userId) {
        when(orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId))
                .thenReturn(Optional.of(new OrgUserBridge()));
    }

    // =========================================================================
    // createCertificate
    // =========================================================================

    @Nested
    @DisplayName("createCertificate")
    class CreateCertificateTests {

        @Test
        @DisplayName("creates certificate with course — returns it")
        void createCertificate_withCourse_success() {
            CreateCertificateRequest request = new CreateCertificateRequest("Safety Cert", 2L, 50L, 100L);
            stubOrgMembership(10L, 1L);
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(testOrg));
            when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
            when(fileObjectRepository.findById(50L)).thenReturn(Optional.of(testFile));
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> {
                Certificate c = inv.getArgument(0);
                c.setId(200L);
                return c;
            });

            Certificate result = certificateService.createCertificate(request, 10L, 1L);

            assertEquals("Safety Cert", result.getCertificateName());
            assertEquals(targetUser, result.getUser());
            assertEquals(testFile, result.getFile());
            assertEquals(testOrg, result.getOrganization());
            assertEquals(testCourse, result.getCourse());
            verify(certificateRepository).save(any(Certificate.class));
        }

        @Test
        @DisplayName("creates certificate without course (null courseId)")
        void createCertificate_withoutCourse_success() {
            CreateCertificateRequest request = new CreateCertificateRequest("External Cert", 2L, 50L, null);
            stubOrgMembership(10L, 1L);
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(testOrg));
            when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
            when(fileObjectRepository.findById(50L)).thenReturn(Optional.of(testFile));
            when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> {
                Certificate c = inv.getArgument(0);
                c.setId(201L);
                return c;
            });

            Certificate result = certificateService.createCertificate(request, 10L, 1L);

            assertNull(result.getCourse());
            verify(courseRepository, never()).findById(any());
        }

        @Test
        @DisplayName("throws when user not in org")
        void createCertificate_userNotInOrg_throws() {
            CreateCertificateRequest request = new CreateCertificateRequest("Cert", 2L, 50L, null);
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> certificateService.createCertificate(request, 10L, 99L));

            verify(certificateRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when organization not found")
        void createCertificate_orgNotFound_throws() {
            CreateCertificateRequest request = new CreateCertificateRequest("Cert", 2L, 50L, null);
            stubOrgMembership(10L, 1L);
            when(organizationRepository.findById(10L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> certificateService.createCertificate(request, 10L, 1L));

            assertEquals("Organization not found", ex.getMessage());
            verify(certificateRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when target user not found")
        void createCertificate_userNotFound_throws() {
            CreateCertificateRequest request = new CreateCertificateRequest("Cert", 2L, 50L, null);
            stubOrgMembership(10L, 1L);
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(testOrg));
            when(userRepository.findById(2L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> certificateService.createCertificate(request, 10L, 1L));

            assertEquals("User not found", ex.getMessage());
            verify(certificateRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when file not found")
        void createCertificate_fileNotFound_throws() {
            CreateCertificateRequest request = new CreateCertificateRequest("Cert", 2L, 50L, null);
            stubOrgMembership(10L, 1L);
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(testOrg));
            when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
            when(fileObjectRepository.findById(50L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> certificateService.createCertificate(request, 10L, 1L));

            assertEquals("File not found", ex.getMessage());
            verify(certificateRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when course not found")
        void createCertificate_courseNotFound_throws() {
            CreateCertificateRequest request = new CreateCertificateRequest("Cert", 2L, 50L, 999L);
            stubOrgMembership(10L, 1L);
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(testOrg));
            when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
            when(fileObjectRepository.findById(50L)).thenReturn(Optional.of(testFile));
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> certificateService.createCertificate(request, 10L, 1L));

            assertEquals("Course not found", ex.getMessage());
            verify(certificateRepository, never()).save(any());
        }
    }

    // =========================================================================
    // getCertificatesForUser (self-service, all orgs)
    // =========================================================================

    @Nested
    @DisplayName("getCertificatesForUser")
    class GetCertificatesForUserTests {

        @Test
        @DisplayName("returns certificates for user across all orgs")
        void getCertificatesForUser_success() {
            when(certificateRepository.findByUserId(1L)).thenReturn(List.of(testCert));

            List<Certificate> result = certificateService.getCertificatesForUser(1L);

            assertEquals(1, result.size());
            assertEquals("Safety Cert", result.get(0).getCertificateName());
        }

        @Test
        @DisplayName("returns empty list when user has no certificates")
        void getCertificatesForUser_empty() {
            when(certificateRepository.findByUserId(1L)).thenReturn(List.of());

            List<Certificate> result = certificateService.getCertificatesForUser(1L);

            assertTrue(result.isEmpty());
        }
    }

    // =========================================================================
    // getCertificatesForOrg
    // =========================================================================

    @Nested
    @DisplayName("getCertificatesForOrg")
    class GetCertificatesForOrgTests {

        @Test
        @DisplayName("returns all certificates for org")
        void getCertificatesForOrg_success() {
            stubOrgMembership(10L, 1L);
            when(certificateRepository.findByOrganizationId(10L)).thenReturn(List.of(testCert));

            List<Certificate> result = certificateService.getCertificatesForOrg(10L, 1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("throws when user not in org")
        void getCertificatesForOrg_notMember_throws() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> certificateService.getCertificatesForOrg(10L, 99L));

            verify(certificateRepository, never()).findByOrganizationId(any());
        }
    }

    // =========================================================================
    // getCertificateById
    // =========================================================================

    @Nested
    @DisplayName("getCertificateById")
    class GetCertificateByIdTests {

        @Test
        @DisplayName("returns certificate when found and belongs to org")
        void getCertificateById_success() {
            stubOrgMembership(10L, 1L);
            when(certificateRepository.findById(200L)).thenReturn(Optional.of(testCert));

            Certificate result = certificateService.getCertificateById(200L, 10L, 1L);

            assertEquals("Safety Cert", result.getCertificateName());
        }

        @Test
        @DisplayName("throws when certificate not found")
        void getCertificateById_notFound_throws() {
            stubOrgMembership(10L, 1L);
            when(certificateRepository.findById(999L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> certificateService.getCertificateById(999L, 10L, 1L));

            assertEquals("Certificate not found", ex.getMessage());
        }

        @Test
        @DisplayName("throws when certificate belongs to different org")
        void getCertificateById_wrongOrg_throws() {
            stubOrgMembership(20L, 1L);
            when(certificateRepository.findById(200L)).thenReturn(Optional.of(testCert));

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> certificateService.getCertificateById(200L, 20L, 1L));

            assertEquals("Certificate not found", ex.getMessage());
        }

        @Test
        @DisplayName("throws when certificate has null organization")
        void getCertificateById_nullOrg_throws() {
            stubOrgMembership(10L, 1L);
            Certificate certNoOrg = new Certificate();
            certNoOrg.setId(300L);
            certNoOrg.setOrganization(null);
            when(certificateRepository.findById(300L)).thenReturn(Optional.of(certNoOrg));

            assertThrows(EntityNotFoundException.class,
                    () -> certificateService.getCertificateById(300L, 10L, 1L));
        }
    }

    // =========================================================================
    // deleteCertificate
    // =========================================================================

    @Nested
    @DisplayName("deleteCertificate")
    class DeleteCertificateTests {

        @Test
        @DisplayName("deletes certificate successfully")
        void deleteCertificate_success() {
            stubOrgMembership(10L, 1L);
            when(certificateRepository.findById(200L)).thenReturn(Optional.of(testCert));

            certificateService.deleteCertificate(200L, 10L, 1L);

            verify(certificateRepository).delete(testCert);
        }

        @Test
        @DisplayName("throws when certificate not found")
        void deleteCertificate_notFound_throws() {
            stubOrgMembership(10L, 1L);
            when(certificateRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> certificateService.deleteCertificate(999L, 10L, 1L));

            verify(certificateRepository, never()).delete(any());
        }

        @Test
        @DisplayName("throws when certificate belongs to different org")
        void deleteCertificate_wrongOrg_throws() {
            stubOrgMembership(20L, 1L);
            when(certificateRepository.findById(200L)).thenReturn(Optional.of(testCert));

            assertThrows(EntityNotFoundException.class,
                    () -> certificateService.deleteCertificate(200L, 20L, 1L));

            verify(certificateRepository, never()).delete(any());
        }
    }

    // =========================================================================
    // getCertificatesForUserInOrg
    // =========================================================================

    @Nested
    @DisplayName("getCertificatesForUserInOrg")
    class GetCertificatesForUserInOrgTests {

        @Test
        @DisplayName("returns certificates for target user in org")
        void getCertificatesForUserInOrg_success() {
            stubOrgMembership(10L, 1L);
            stubOrgMembership(10L, 2L);
            when(certificateRepository.findByUserIdAndOrganizationId(2L, 10L)).thenReturn(List.of(testCert));

            List<Certificate> result = certificateService.getCertificatesForUserInOrg(2L, 10L, 1L);

            assertEquals(1, result.size());
            assertEquals("Safety Cert", result.get(0).getCertificateName());
        }

        @Test
        @DisplayName("throws when requesting user not in org")
        void getCertificatesForUserInOrg_requesterNotInOrg_throws() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> certificateService.getCertificatesForUserInOrg(2L, 10L, 99L));

            verify(certificateRepository, never()).findByUserIdAndOrganizationId(any(), any());
        }

        @Test
        @DisplayName("throws when target user not in org")
        void getCertificatesForUserInOrg_targetNotInOrg_throws() {
            stubOrgMembership(10L, 1L);
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> certificateService.getCertificatesForUserInOrg(99L, 10L, 1L));

            verify(certificateRepository, never()).findByUserIdAndOrganizationId(any(), any());
        }
    }

    // =========================================================================
    // updateCertificate
    // =========================================================================

    @Nested
    @DisplayName("updateCertificate")
    class UpdateCertificateTests {

        @Test
        @DisplayName("updates all provided fields")
        void updateCertificate_allFields() {
            FileObject newFile = new FileObject();
            ReflectionTestUtils.setField(newFile, "id", 60L);
            Course newCourse = new Course();
            newCourse.setId(101L);

            UpdateCertificateRequest request = new UpdateCertificateRequest("Updated Name", 60L, 101L);
            stubOrgMembership(10L, 1L);
            when(certificateRepository.findById(200L)).thenReturn(Optional.of(testCert));
            when(fileObjectRepository.findById(60L)).thenReturn(Optional.of(newFile));
            when(courseRepository.findById(101L)).thenReturn(Optional.of(newCourse));
            when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));

            Certificate result = certificateService.updateCertificate(200L, request, 10L, 1L);

            assertEquals("Updated Name", result.getCertificateName());
            assertEquals(newFile, result.getFile());
            assertEquals(newCourse, result.getCourse());
        }

        @Test
        @DisplayName("partial update — only name, file and course unchanged")
        void updateCertificate_onlyName() {
            UpdateCertificateRequest request = new UpdateCertificateRequest("New Name", null, null);
            stubOrgMembership(10L, 1L);
            when(certificateRepository.findById(200L)).thenReturn(Optional.of(testCert));
            when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));

            Certificate result = certificateService.updateCertificate(200L, request, 10L, 1L);

            assertEquals("New Name", result.getCertificateName());
            assertEquals(testFile, result.getFile());
            assertEquals(testCourse, result.getCourse());
            verify(fileObjectRepository, never()).findById(any());
            verify(courseRepository, never()).findById(any());
        }

        @Test
        @DisplayName("throws when certificate not found")
        void updateCertificate_notFound_throws() {
            UpdateCertificateRequest request = new UpdateCertificateRequest("Name", null, null);
            stubOrgMembership(10L, 1L);
            when(certificateRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> certificateService.updateCertificate(999L, request, 10L, 1L));

            verify(certificateRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when certificate belongs to different org")
        void updateCertificate_wrongOrg_throws() {
            UpdateCertificateRequest request = new UpdateCertificateRequest("Name", null, null);
            stubOrgMembership(20L, 1L);
            when(certificateRepository.findById(200L)).thenReturn(Optional.of(testCert));

            assertThrows(EntityNotFoundException.class,
                    () -> certificateService.updateCertificate(200L, request, 20L, 1L));

            verify(certificateRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when new file not found")
        void updateCertificate_fileNotFound_throws() {
            UpdateCertificateRequest request = new UpdateCertificateRequest(null, 999L, null);
            stubOrgMembership(10L, 1L);
            when(certificateRepository.findById(200L)).thenReturn(Optional.of(testCert));
            when(fileObjectRepository.findById(999L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> certificateService.updateCertificate(200L, request, 10L, 1L));

            assertEquals("File not found", ex.getMessage());
        }

        @Test
        @DisplayName("throws when new course not found")
        void updateCertificate_courseNotFound_throws() {
            UpdateCertificateRequest request = new UpdateCertificateRequest(null, null, 999L);
            stubOrgMembership(10L, 1L);
            when(certificateRepository.findById(200L)).thenReturn(Optional.of(testCert));
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> certificateService.updateCertificate(200L, request, 10L, 1L));

            assertEquals("Course not found", ex.getMessage());
        }
    }
}
