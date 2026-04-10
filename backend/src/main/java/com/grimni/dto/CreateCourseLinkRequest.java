package com.grimni.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCourseLinkRequest(
    @NotBlank(message = "Link cannot be blank")
    String link
) {}
