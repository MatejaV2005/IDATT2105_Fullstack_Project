package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrganizationRequest(
    @NotBlank(message = "Organization name cannot be blank")
    String orgName,

    String orgAddress,

    @NotNull(message = "Organization number is required")
    @Positive(message = "Organization number must be positive")
    int orgNumber,

    
    boolean alcoholEnabled,
    boolean foodEnabled
) {}