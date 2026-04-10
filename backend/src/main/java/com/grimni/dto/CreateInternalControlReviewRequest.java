package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateInternalControlReviewRequest(
    @NotBlank String summary
) {
}
