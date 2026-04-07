package com.grimni.dto;

import jakarta.validation.constraints.Size;

public record UpdateCertificateRequest(
    @Size(min = 1, message = "Certificate name cannot be empty")
    String certificateName,

    Long fileId,

    Long courseId
) {}
