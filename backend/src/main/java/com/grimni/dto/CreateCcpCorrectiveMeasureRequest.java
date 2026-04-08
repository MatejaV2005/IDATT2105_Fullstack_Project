package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCcpCorrectiveMeasureRequest(
    @NotNull(message = "Product category id is required")
    Long productCategoryId,

    @NotBlank(message = "Measure description cannot be blank")
    String measureDescription
) {}
