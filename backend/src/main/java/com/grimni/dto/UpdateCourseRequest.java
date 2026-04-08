package com.grimni.dto;

import jakarta.validation.constraints.Size;

public record UpdateCourseRequest(
    @Size(min = 1, message = "Title cannot be empty")
    String title,

    String courseDescription
) {}
