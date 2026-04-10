package com.grimni.service;

import java.io.InputStream;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Service for managing low-level file storage operations using AWS S3.
 * <p>
 * This service provides a thin wrapper around the {@link S3Client} to facilitate 
 * uploading files either from a local filesystem path or directly from an 
 * {@link InputStream}. It acts as the primary integration point between the 
 * application and cloud storage providers.
 * </p>
 */
@Service
public class FileStorageService {

    private final S3Client s3Client;

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Uploads a file to a specified S3 bucket from a local file path.
     * <p>
     * Use this method when the file already exists on the local disk. The AWS SDK 
     * handles the file reading and buffering automatically.
     * </p>
     *
     * @param bucket   the name of the target S3 bucket.
     * @param key      the unique key (path) under which the file will be stored in the bucket.
     * @param filePath the {@link Path} to the local file to be uploaded.
     * @throws software.amazon.awssdk.services.s3.model.S3Exception if the upload fails due to AWS service issues.
     */
    public void upload(String bucket, String key, Path filePath) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build(),
            RequestBody.fromFile(filePath)
        );
    }

    /**
     * Uploads a file to a specified S3 bucket using an input stream.
     * <p>
     * Use this method for streaming data, such as multipart file uploads from a REST controller, 
     * where the content length is known in advance.
     * </p>
     *
     * @param bucket        the name of the target S3 bucket.
     * @param key           the unique key (path) under which the file will be stored.
     * @param inputStream   the source stream containing the file data.
     * @param contentLength the size of the data in bytes (required for S3 uploads).
     * @param contentType   the MIME type of the file (e.g., "application/pdf", "image/png").
     * @throws software.amazon.awssdk.services.s3.model.S3Exception if the upload fails.
     */
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