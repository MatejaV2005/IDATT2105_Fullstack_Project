package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductCategoryRequest(
    @NotBlank(message = "productName is required")
    @Size(max = 500, message = "productName is too long")
    String productName,

    @NotBlank(message = "productDescription is required")
    @Size(max = 5000, message = "productDescription is too long")
    String productDescription
) {}
