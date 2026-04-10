package com.grimni.dto;

public record TeamUserOverviewResponse(
    Long userId,
    String legalName,
    String orgRole,
    TeamAssignmentsResponse ccpAssignments,
    TeamAssignmentsResponse routineAssignments,
    int mappingPointResponsibilities,
    long openReviewedCcpDeviations,
    long openReviewedRoutineDeviations,
    TeamCourseProgressResponse courseProgress
) {
}
