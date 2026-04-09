package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateMappingPointRequest(
    @NotBlank(message = "Law cannot be blank")
    String law,

    @NotNull(message = "Dots is required")
    @PositiveOrZero(message = "Dots must be zero or positive")
    Short dots,

    @NotBlank(message = "Title cannot be blank")
    String title,

    @NotBlank(message = "Challenges cannot be blank")
    String challenges,

    @NotBlank(message = "Measures cannot be blank")
    String measures,

    @NotBlank(message = "Responsible for point cannot be blank")
    String responsibleForPoint
) {}
