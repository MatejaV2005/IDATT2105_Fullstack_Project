package com.grimni.dto;

import com.grimni.domain.enums.VerificationStatus;

import jakarta.validation.constraints.NotNull;

public record SetVerificationStatusRequest(
    @NotNull Long id,
    @NotNull VerificationStatus verificationStatus
) {
}
