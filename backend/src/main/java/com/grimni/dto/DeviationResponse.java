package com.grimni.dto;

import com.grimni.domain.Deviation;
import com.grimni.domain.enums.DeviationCategory;
import com.grimni.domain.enums.ReviewStatus;

import java.time.LocalDateTime;

public record DeviationResponse(
    Long id,
    Long organizationId,
    Long reportedById,
    String reportedByName,
    DeviationCategory category,
    ReviewStatus reviewStatus,
    LocalDateTime createdAt,
    Long reviewedById,
    String reviewedByName,
    LocalDateTime reviewedAt,
    String whatWentWrong,
    String immediateActionTaken,
    String potentialCause,
    String potentialPreventativeMeasure,
    String preventativeMeasureActuallyTaken
) {
    public static DeviationResponse fromEntity(Deviation deviation) {
        return new DeviationResponse(
            deviation.getId(),
            deviation.getOrganization() != null ? deviation.getOrganization().getId() : null,
            deviation.getReportedBy() != null ? deviation.getReportedBy().getId() : null,
            deviation.getReportedBy() != null ? deviation.getReportedBy().getLegalName() : null,
            deviation.getCategory(),
            deviation.getReviewStatus(),
            deviation.getCreatedAt(),
            deviation.getReviewedBy() != null ? deviation.getReviewedBy().getId() : null,
            deviation.getReviewedBy() != null ? deviation.getReviewedBy().getLegalName() : null,
            deviation.getReviewedAt(),
            deviation.getWhatWentWrong(),
            deviation.getImmediateActionTaken(),
            deviation.getPotentialCause(),
            deviation.getPotentialPreventativeMeasure(),
            deviation.getPreventativeMeasureActuallyTaken()
        );
    }
}
