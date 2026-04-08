package com.grimni.service;

import java.io.InputStream;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class FileStorageService {
    private final S3Client s3Client;

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void upload(String bucket, String key, Path filePath) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build(),
            RequestBody.fromFile(filePath)
        );
    }

    public void upload(String bucket, String key, InputStream inputStream, long contentLength, String contentType) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build(),
            RequestBody.fromInputStream(inputStream, contentLength)
        );
    }
}
