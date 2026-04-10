package com.grimni.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
    @Size(min = 1, message = "Organization name cannot be empty")
    String orgName,

    String orgAddress,

    @Positive(message = "Organization number must be positive")
    Integer orgNumber,

    Boolean alcoholEnabled,

    Boolean foodEnabled
) {}