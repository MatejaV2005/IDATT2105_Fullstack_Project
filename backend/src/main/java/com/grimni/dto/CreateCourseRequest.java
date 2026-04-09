package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCourseRequest(
    @NotBlank(message = "Title cannot be blank")
    String title,

    @NotBlank(message = "Course description cannot be blank")
    String courseDescription
) {}
