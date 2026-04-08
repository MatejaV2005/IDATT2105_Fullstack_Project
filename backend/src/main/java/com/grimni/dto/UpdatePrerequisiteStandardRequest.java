package com.grimni.dto;

import jakarta.validation.constraints.Size;

public record UpdatePrerequisiteStandardRequest(
    @Size(min = 1, message = "Title cannot be empty")
    String title,

    @Size(min = 1, message = "Description cannot be empty")
    String description,

    Long categoryId
) {}
