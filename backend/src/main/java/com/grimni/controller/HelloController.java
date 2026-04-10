package com.grimni.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.service.FileStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Provides a simple health-check endpoint for liveness probes.
 */
@Tag(name = "Health", description = "Application health check")
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

   /** Returns a simple status object indicating the application is running. */
   @Operation(summary = "Health check", description = "Returns {status: ok} when the application is alive")
   @GetMapping("/health")
   public Map<String, String> health() {
       return Map.of("status", "ok");
    }
}