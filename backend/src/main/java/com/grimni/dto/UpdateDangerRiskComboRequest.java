package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateDangerRiskComboRequest(
    @NotNull(message = "productCategoryId is required")
    Long productCategoryId,

    @NotNull(message = "dangerRiskComboId is required")
    Long dangerRiskComboId,

    @NotBlank(message = "danger cannot be blank")
    String danger,

    @NotBlank(message = "dangerCorrectiveMeasure cannot be blank")
    String dangerCorrectiveMeasure,

    @NotNull(message = "severityScore is required")
    @PositiveOrZero(message = "severityScore must be zero or positive")
    Integer severityScore,

    @NotNull(message = "likelihoodScore is required")
    @PositiveOrZero(message = "likelihoodScore must be zero or positive")
    Integer likelihoodScore
) {}
