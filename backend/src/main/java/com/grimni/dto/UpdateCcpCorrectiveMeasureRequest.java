package com.grimni.dto;

import jakarta.validation.constraints.Size;

public record UpdateCcpCorrectiveMeasureRequest(
    Long productCategoryId,

    @Size(min = 1, message = "Measure description cannot be empty")
    String measureDescription
) {}
