package com.grimni.dto;

import java.time.LocalDateTime;

import com.grimni.domain.InternalControlReview;

public record InternalControlReviewResponse(
    Long id,
    Long organizationId,
    Long reviewedById,
    String reviewedByName,
    String summary,
    LocalDateTime createdAt
) {
    public static InternalControlReviewResponse fromEntity(InternalControlReview review) {
        return new InternalControlReviewResponse(
            review.getId(),
            review.getOrganization() != null ? review.getOrganization().getId() : null,
            review.getReviewedBy() != null ? review.getReviewedBy().getId() : null,
            review.getReviewedBy() != null ? review.getReviewedBy().getLegalName() : null,
            review.getSummary(),
            review.getCreatedAt()
        );
    }
}
