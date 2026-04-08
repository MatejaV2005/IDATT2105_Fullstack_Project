package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePrerequisiteCategoryRequest(
    @NotBlank(message = "Category name cannot be blank")
    String categoryName
) {}
