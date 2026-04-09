package com.grimni.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdatePrerequisiteRoutineRequest(
    @Size(min = 1, message = "Title cannot be empty")
    String title,

    @Size(min = 1, message = "Description cannot be empty")
    String description,

    @Size(min = 1, message = "Measures cannot be empty")
    String measures,

    Long categoryId,

    @PositiveOrZero(message = "Interval start must be zero or positive")
    Long intervalStart,

    @Positive(message = "Interval repeat time must be positive")
    Long intervalRepeatTime
) {}
