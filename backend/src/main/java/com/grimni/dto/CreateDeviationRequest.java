package com.grimni.dto;

import com.grimni.domain.enums.DeviationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDeviationRequest(
    @NotNull Long organizationId,
    Long ccpRecordId,
    Long routineRecordId,
    @NotNull DeviationCategory category,
    @NotBlank String whatWentWrong,
    @NotBlank String immediateActionTaken,
    @NotBlank String potentialCause,
    @NotBlank String potentialPreventativeMeasure
) {
}