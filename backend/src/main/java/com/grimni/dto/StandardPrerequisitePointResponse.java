package com.grimni.dto;

public record StandardPrerequisitePointResponse(
    Long id,
    String title,
    String type,
    String description
) implements PrerequisitePointResponse {
}
