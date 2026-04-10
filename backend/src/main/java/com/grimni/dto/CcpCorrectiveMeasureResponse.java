package com.grimni.dto;

public record CcpCorrectiveMeasureResponse(
    Long id,
    Long productCategoryId,
    String productName,
    String measureDescription
) {}
