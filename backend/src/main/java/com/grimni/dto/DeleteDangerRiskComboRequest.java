package com.grimni.dto;

import jakarta.validation.constraints.NotNull;

public record DeleteDangerRiskComboRequest(
    @NotNull(message = "dangerRiskComboId is required")
    Long dangerRiskComboId
) {}
