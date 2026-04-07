package com.grimni.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.grimni.domain.FileObject;
import com.grimni.domain.Organization;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.User;
import com.grimni.domain.enums.AccessLevel;
import com.grimni.repository.UserRepository;
import com.grimni.service.SimpleStorageService;
import com.grimni.util.JwtUtil;

@RestController
@RequestMapping("/e-files")
public class GrimniFileController {
    private static final Logger logger = LoggerFactory.getLogger(GrimniFileController.class);

    private final SimpleStorageService simpleStorageService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final String bucket;

    public GrimniFileController(
        SimpleStorageService simpleStorageService,
        UserRepository userRepository,
        JwtUtil jwtUtil,
        @Value("${s3.bucket}") String bucket
    ) {
        this.simpleStorageService = simpleStorageService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.bucket = bucket;
    }

    @GetMapping("/{fileObjectId}")
    public ResponseEntity<byte[]> read(@PathVariable Long fileObjectId) {
        User user = getCurrentUser();

        try {
            SimpleStorageService.StoredFile storedFile = simpleStorageService.read(fileObjectId, user.getOrganizations());
            FileObject fileObject = storedFile.fileObject();

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storedFile.contentType()))
                .header("Content-Disposition", "attachment; filename=\"" + fileObject.getFileName() + "\"")
                .body(storedFile.bytes());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File wasn't found", exception);
        } catch (SecurityException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
        }
    }

    @DeleteMapping("/{fileObjectId}")
    public ResponseEntity<Void> delete(@PathVariable Long fileObjectId) {
        User user = getCurrentUser();

        try {
            simpleStorageService.delete(fileObjectId, user.getOrganizations());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File wasn't found", exception);
        } catch (SecurityException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
        }
    }
    
    @PostMapping
    public ResponseEntity<String> upload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("orgId") Long orgId
    ) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        User uploadedBy = null;
        try {
            Long userId = jwtUtil.getAuthenticatedUserId();
            uploadedBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
        }

        Organization organization = uploadedBy.getOrganizations().stream()
            .map(OrgUserBridge::getOrganization)
            .filter(org -> org != null && orgId.equals(org.getId()))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User isn't in organization"));

        String originalFilename = file.getOriginalFilename() == null ? "unnamed" : StringUtils.cleanPath(file.getOriginalFilename());
        String key = "sanity/" + UUID.randomUUID() + "-" + originalFilename;

        try {
            simpleStorageService.upload(
                key,
                file.getInputStream(),
                file.getSize(),
                file.getContentType(),
                AccessLevel.OWNER,
                AccessLevel.OWNER,
                AccessLevel.OWNER,
                uploadedBy,
                originalFilename,
                organization
            );
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        } catch (RuntimeException exception) {
            logger.error("S3 upload failed for key {}", key, exception);
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "S3 upload failed: " + exception.getMessage(),
                exception
            );
        } catch (java.io.IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", exception);
        }

        return ResponseEntity.ok("Uploaded to bucket '" + bucket + "' with key: " + key);
    }

    private User getCurrentUser() {
        try {
            Long userId = jwtUtil.getAuthenticatedUserId();
            return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
        }
    }
}
