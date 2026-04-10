package com.grimni.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreatePrerequisiteRoutineRequest(
    @NotNull(message = "Category ID is required")
    Long categoryId,

    @NotBlank(message = "Title cannot be blank")
    String title,

    @NotBlank(message = "Description cannot be blank")
    String description,

    @NotBlank(message = "Measures cannot be blank")
    String measures,

    @NotNull(message = "Interval start is required")
    @PositiveOrZero(message = "Interval start must be zero or positive")
    Long intervalStart,

    @NotNull(message = "Interval repeat time is required")
    @Positive(message = "Interval repeat time must be positive")
    Long intervalRepeatTime,

    List<Long> verifierUserIds,
    List<Long> deviationReceiverUserIds,
    List<Long> performerUserIds,
    List<Long> deputyUserIds
) {}
