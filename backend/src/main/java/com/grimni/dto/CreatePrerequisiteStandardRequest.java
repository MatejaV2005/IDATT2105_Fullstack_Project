package com.grimni.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePrerequisiteStandardRequest(
    @NotNull Long categoryId,

    @NotBlank(message = "Title cannot be blank")
    String title,

    @NotBlank(message = "Description cannot be blank")
    String description,

    List<String> links,

    List<String> resources
) {}
