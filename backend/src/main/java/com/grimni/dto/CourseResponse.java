package com.grimni.dto;

import java.time.LocalDateTime;

import com.grimni.domain.Course;

public record CourseResponse(
    Long id,
    String title,
    String courseDescription,
    Long organizationId,
    LocalDateTime createdAt
) {
    public static CourseResponse fromEntity(Course course) {
        return new CourseResponse(
            course.getId(),
            course.getTitle(),
            course.getCourseDescription(),
            course.getOrganization().getId(),
            course.getCreatedAt()
        );
    }
}
