package com.grimni.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateMappingPointRequest(
    @Size(min = 1, message = "Law cannot be empty")
    String law,

    @PositiveOrZero(message = "Dots must be zero or positive")
    Short dots,

    @Size(min = 1, message = "Title cannot be empty")
    String title,

    @Size(min = 1, message = "Challenges cannot be empty")
    String challenges,

    @Size(min = 1, message = "Measures cannot be empty")
    String measures,

    @Size(min = 1, message = "Responsible text cannot be empty")
    String responsibleText
) {}
