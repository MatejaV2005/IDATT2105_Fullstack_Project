package com.grimni.dto;

import java.util.List;

public record CourseOverviewResponse(
    List<CourseDetailResponse> allCourses,
    List<UserProgressOverview> userProgress
) {

    public record CourseDetailResponse(
        Long id,
        String title,
        String courseDescription,
        List<ResponsibleUserResponse> responsibleUsers,
        List<CourseResourceResponse> resources
    ) {}

    public record ResponsibleUserResponse(
        Long userId,
        String legalName
    ) {}

    public record CourseResourceResponse(
        Long id,
        String type,
        String name
    ) {}

    public record UserProgressOverview(
        Long userId,
        String legalName,
        List<UserCourseStatus> courses
    ) {}

    public record UserCourseStatus(
        Long courseId,
        String title,
        Boolean completed
    ) {}
}
