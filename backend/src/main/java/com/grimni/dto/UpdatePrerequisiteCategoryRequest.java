package com.grimni.dto;

import jakarta.validation.constraints.Size;

public record UpdatePrerequisiteCategoryRequest(
    @Size(min = 1, message = "Category name cannot be empty")
    String categoryName
) {}
