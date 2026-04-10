package com.grimni.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.domain.Course;
import com.grimni.domain.FileCourseBridge;
import com.grimni.domain.FileObject;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.AccessLevel;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.domain.ids.FileCourseBridgeId;
import com.grimni.repository.CourseRepository;
import com.grimni.repository.FileCourseBridgeRepository;
import com.grimni.repository.FileObjectRepository;

import jakarta.persistence.EntityNotFoundException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Service for high-level blob storage management and organizational file access control.
 * <p>
 * This service acts as an abstraction layer over AWS S3, providing:
 * <ul>
 * <li>Secure file uploads with automatic database synchronization.</li>
 * <li>Granular access control based on organizational roles and {@link AccessLevel}.</li>
 * <li>Atomicity between cloud storage and database records (cleanup on persistence failure).</li>
 * <li>Logic for linking file assets to educational {@link Course} entities.</li>
 * </ul>
 */
@Service
public class SimpleStorageService {

    /**
     * DTO representing a retrieved file, containing its metadata and raw byte content.
     *
     * @param fileObject  The database metadata entity.
     * @param bytes       The raw file content retrieved from storage.
     * @param contentType The resolved MIME type of the file.
     */
    public record StoredFile(
        FileObject fileObject,
        byte[] bytes,
        String contentType
    ) {}

    private final S3Client s3Client;
    private final FileObjectRepository repository;
    private final CourseRepository courseRepository;
    private final FileCourseBridgeRepository fileCourseBridgeRepository;
    private final String bucket;

    public SimpleStorageService(
        S3Client s3Client,
        FileObjectRepository repository,
        CourseRepository courseRepository,
        FileCourseBridgeRepository fileCourseBridgeRepository,
        @Value("${s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.repository = repository;
        this.courseRepository = courseRepository;
        this.fileCourseBridgeRepository = fileCourseBridgeRepository;
        this.bucket = bucket;
    }

    /**
     * Uploads a file stream to S3 and persists the corresponding metadata to the database.
     * <p>
     * If the database persistence fails, a compensation logic triggers to delete the orphaned 
     * object from S3, ensuring data consistency across the distributed system.
     *
     * @param key           The unique object key (path) in S3.
     * @param inputStream   The source data stream.
     * @param contentLength The length of the stream in bytes.
     * @param contentType   The MIME type of the file.
     * @param readAccess    Threshold {@link AccessLevel} required to retrieve the file.
     * @param deleteAccess  Threshold {@link AccessLevel} required to remove the file.
     * @param uploadedBy    The {@link User} entity performing the upload.
     * @param fileName      The original human-readable filename.
     * @param org           The {@link Organization} the file belongs to.
     * @return The persisted {@link FileObject} metadata entity.
     * @throws RuntimeException if persistence fails, triggering S3 cleanup.
     */
    @Transactional
    public FileObject upload(
        String key, 
        InputStream inputStream, 
        long contentLength, 
        String contentType,
        AccessLevel readAccess,
        AccessLevel deleteAccess,
        User uploadedBy,
        String fileName,
        Organization org
    ) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(this.bucket)
                .key(key)
                .contentType(contentType)
                .build(),
            RequestBody.fromInputStream(inputStream, contentLength)
        );

        FileObject file = new FileObject();
        file.setUploadedBy(uploadedBy);
        file.setFileName(fileName);
        file.setOrganization(org);
        file.setObjectKey(key);
        file.setReadAccess(readAccess);
        file.setDeleteAccess(deleteAccess);

        try {
            return repository.save(file);
        } catch (RuntimeException exception) {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(key)
                    .build()
            );
            throw exception;
        }
    }

    /**
     * Deletes a file from both the database and S3 storage.
     * <p>
     * Access is strictly enforced based on the {@code deleteAccess} level defined on the file.
     *
     * @param fileObjectId  The database ID of the file to delete.
     * @param orgUserBridge The membership details of the user performing the deletion.
     * @throws EntityNotFoundException if the file metadata does not exist.
     * @throws SecurityException       if the user lacks the required permission level.
     */
    @Transactional
    public void delete(
        Long fileObjectId,
        List<OrgUserBridge> orgUserBridge
    ) {
        FileObject fileObject = repository.findById(fileObjectId).orElseThrow(() -> new EntityNotFoundException("File object not found"));
        AccessLevel fileObjectAccessLevel = fileObject.getDeleteAccess();
        Long fileObjectOrgId = fileObject.getOrganization().getId();
        if (!doesUserHaveRightAccessLevel(fileObjectOrgId, fileObjectAccessLevel, orgUserBridge)) {
            throw new SecurityException("You don't have access level to delete this file");
        }


        String objectKey = fileObject.getObjectKey();
        repository.delete(fileObject);
        repository.flush();

        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(this.bucket)
                .key(objectKey)
                .build()
        );
    }

    /**
     * Retrieves a file's content and metadata from the system.
     *
     * @param fileObjectId  The unique identifier of the file.
     * @param orgUserBridge The membership context of the user requesting the file.
     * @return A {@link StoredFile} containing the raw content and resolved metadata.
     * @throws SecurityException if the user does not meet the {@code readAccess} requirements.
     */
    @Transactional(readOnly = true)
    public StoredFile read(Long fileObjectId, List<OrgUserBridge> orgUserBridge) {
        FileObject fileObject = repository.findById(fileObjectId).orElseThrow(() -> new EntityNotFoundException("File object not found"));
        AccessLevel fileObjectAccessLevel = fileObject.getReadAccess();
        Long fileObjectOrgId = fileObject.getOrganization().getId();
        if (!doesUserHaveRightAccessLevel(fileObjectOrgId, fileObjectAccessLevel, orgUserBridge)) {
            throw new SecurityException("You don't have access level to read this file");
        }

        String objectKey = fileObject.getObjectKey();
        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
            GetObjectRequest.builder()
                .bucket(this.bucket)
                .key(objectKey)
                .build()
        );

        String contentType = objectBytes.response().contentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return new StoredFile(fileObject, objectBytes.asByteArray(), contentType);
    }

    /**
     * Creates a logical link between a stored file and a specific course.
     *
     * @param fileId   The unique identifier of the file.
     * @param courseId The unique identifier of the course.
     * @throws EntityNotFoundException if the file or course cannot be located.
     */
    @Transactional
    public void linkFileToCourse(Long fileId, Long courseId) {
        FileObject file = repository.findById(fileId)
            .orElseThrow(() -> new EntityNotFoundException("File not found"));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        FileCourseBridge bridge = new FileCourseBridge();
        bridge.setId(new FileCourseBridgeId(courseId, fileId));
        bridge.setCourse(course);
        bridge.setFile(file);
        fileCourseBridgeRepository.save(bridge);
    }

    /**
     * Evaluates whether the requester's organizational role meets the required access threshold.
     *
     * @param fileObjectOrgId      The ID of the organization that owns the file.
     * @param fileObjectAccessLevel The {@link AccessLevel} required for the operation.
     * @param orgUserBridge        The collection of organization memberships for the current user.
     * @return {@code true} if access is permitted; {@code false} otherwise.
     */
    private boolean doesUserHaveRightAccessLevel(Long fileObjectOrgId, AccessLevel fileObjectAccessLevel, List<OrgUserBridge> orgUserBridge) {
        if (orgUserBridge == null) {
            return fileObjectAccessLevel.equals(AccessLevel.PUBLIC);
        }

        Boolean isUserInOrg = orgUserBridge.stream().map(OrgUserBridge::getOrganization).filter(org -> org.getId().equals(fileObjectOrgId)).count() > 0;
        if (fileObjectAccessLevel.equals(AccessLevel.PUBLIC)) return true;
        if (!isUserInOrg) return false;
        if (fileObjectAccessLevel.equals(AccessLevel.ANYONE_IN_ORG)) return true;
        List<OrgUserRole> userRolesInOrg = orgUserBridge.stream()
            .filter(bridge -> bridge.getOrganization().getId().equals(fileObjectOrgId))
            .map(bridge -> bridge.getUserRole())
            .toList();
        if (userRolesInOrg.contains(OrgUserRole.OWNER)) return true;
        if (!userRolesInOrg.contains(OrgUserRole.OWNER) && fileObjectAccessLevel.equals(AccessLevel.OWNER)) return false;
        if (!userRolesInOrg.contains(OrgUserRole.MANAGER) && fileObjectAccessLevel.equals(AccessLevel.MANAGER)) return false;
        return true;
    }
}