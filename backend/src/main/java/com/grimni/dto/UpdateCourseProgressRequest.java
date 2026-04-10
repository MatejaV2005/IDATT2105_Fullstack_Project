package com.grimni.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCourseProgressRequest(
    @NotNull Long userId,
    @NotNull Long courseId,
    @NotNull Boolean completed
) {
}
