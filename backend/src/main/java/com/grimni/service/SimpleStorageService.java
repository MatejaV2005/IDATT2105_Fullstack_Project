package com.grimni.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.domain.FileObject;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.AccessLevel;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.repository.FileObjectRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class SimpleStorageService {
    public record StoredFile(
        FileObject fileObject,
        byte[] bytes,
        String contentType
    ) {}

    private final S3Client s3Client;
    private final FileObjectRepository repository;
    private final String bucket;

    public SimpleStorageService(
        S3Client s3Client,
        FileObjectRepository repository,
        @Value("${s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.repository = repository;
        this.bucket = bucket;
    }

    @Transactional
    public FileObject upload(
        String key, 
        InputStream inputStream, 
        long contentLength, 
        String contentType, // should we perhaps not let the client set this?
        AccessLevel createAccess,
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
        file.setCreateAccess(createAccess);
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

    @Transactional
    public void delete(
        Long fileObjectId,
        List<OrgUserBridge> orgUserBridge
    ) {
        FileObject fileObject = repository.findById(fileObjectId).orElseThrow(() -> new IllegalArgumentException("File object not found"));
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

    @Transactional(readOnly = true)
    public StoredFile read(Long fileObjectId, List<OrgUserBridge> orgUserBridge) {
        FileObject fileObject = repository.findById(fileObjectId).orElseThrow(() -> new IllegalArgumentException("File object not found"));
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
