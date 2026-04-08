package com.grimni.dto;

import java.util.List;

public record PrerequisiteCategoryAllInfoResponse(
    Long id,
    String categoryName,
    List<PrerequisitePointResponse> points
) {}
