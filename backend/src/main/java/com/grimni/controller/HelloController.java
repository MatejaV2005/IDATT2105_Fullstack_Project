package com.grimni.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.service.FileStorageService;

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

@RestController
public class HelloController {

    private final FileStorageService fileStorageService;
    private final String bucket;

    public HelloController(
        FileStorageService fileStorageService,
        @Value("${s3.bucket}") String bucket
    ) {
        this.fileStorageService = fileStorageService;
        this.bucket = bucket;
    }

   @GetMapping("/health")
   public Map<String, String> health() {
       return Map.of("status", "ok");
    }
    
    @GetMapping("/test-upload-from-backend")
    public Map<String, String> testUploadFromBackend() {

        File file = new File("/schema.sql");
        fileStorageService.upload(bucket, "banana", file.toPath());
        return Map.of("status", "ok");
   }
}