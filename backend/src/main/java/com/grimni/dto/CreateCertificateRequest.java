package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCertificateRequest(
    @NotBlank(message = "Certificate name cannot be blank")
    String certificateName,

    @NotNull(message = "User ID is required")
    Long userId,

    @NotNull(message = "File ID is required")
    Long fileId,

    Long courseId
) {}
