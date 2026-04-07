package com.grimni.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.service.FileStorageService;


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
}