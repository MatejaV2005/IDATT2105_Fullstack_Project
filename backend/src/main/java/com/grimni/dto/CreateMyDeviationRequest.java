package com.grimni.dto;

import com.grimni.domain.enums.DeviationCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMyDeviationRequest(
    Long ccpRecordId,
    Long routineRecordId,
    @NotNull DeviationCategory category,
    @NotBlank String whatWentWrong,
    @NotBlank String immediateActionTaken,
    @NotBlank String potentialCause,
    @NotBlank String potentialPreventativeMeasure
) {
    public CreateDeviationRequest toCreateDeviationRequest(Long organizationId) {
        return new CreateDeviationRequest(
            organizationId,
            ccpRecordId,
            routineRecordId,
            category,
            whatWentWrong,
            immediateActionTaken,
            potentialCause,
            potentialPreventativeMeasure
        );
    }
}
