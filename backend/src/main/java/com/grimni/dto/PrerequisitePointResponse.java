package com.grimni.dto;

public sealed interface PrerequisitePointResponse
    permits RoutinePrerequisitePointResponse, StandardPrerequisitePointResponse {

    Long id();

    String title();

    String type();
}
