package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePrerequisiteStandardRequest(
    @NotBlank(message = "Title cannot be blank")
    String title,

    @NotBlank(message = "Description cannot be blank")
    String description
) {}
