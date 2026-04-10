package com.grimni.util;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuration class for the Amazon S3 client.
 * <p>
 * This class defines the Spring bean for {@link S3Client}, utilizing AWS SDK v2. 
 * It supports flexible connection settings, allowing the application to connect to 
 * official AWS S3 regions or S3-compatible storage providers (like MinIO) via 
 * endpoint overrides and path-style access.
 * </p>
 */
@Configuration
public class S3Config {

    /**
     * Creates and configures an {@link S3Client} bean.
     * <p>
     * The client is initialized with credentials and connection parameters 
     * injected from the application properties.
     * </p>
     *
     * @param endpoint       The service endpoint URI (e.g., for MinIO or localstack).
     * @param accessKey      The AWS access key ID.
     * @param secretKey      The AWS secret access key.
     * @param region         The AWS region string (e.g., "eu-north-1").
     * @param forcePathStyle Boolean to determine if bucket names should be part of the 
     * URL path rather than a subdomain (required by some S3-compatible APIs).
     * @return a thread-safe {@link S3Client} instance.
     */
    @Bean
    public S3Client s3Client(
        @Value("${s3.endpoint}") String endpoint,
        @Value("${s3.access-key}") String accessKey,
        @Value("${s3.secret-key}") String secretKey,
        @Value("${s3.region}") String region,
        @Value("${s3.force-path-style}") Boolean forcePathStyle
    ) {
        return S3Client.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .forcePathStyle(forcePathStyle)
            .build();   
    }
}