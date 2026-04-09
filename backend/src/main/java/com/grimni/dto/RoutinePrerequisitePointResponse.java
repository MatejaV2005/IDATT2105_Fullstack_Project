package com.grimni.dto;

import java.util.List;

public record RoutinePrerequisitePointResponse(
    Long id,
    String title,
    String type,
    String description,
    String measures,
    String repeatText,
    PrerequisiteIntervalResponse interval,
    List<PrerequisiteUserResponse> verifiers,
    List<PrerequisiteUserResponse> deviationReceivers,
    List<PrerequisiteUserResponse> performers,
    List<PrerequisiteUserResponse> deputy
) implements PrerequisitePointResponse {
}
