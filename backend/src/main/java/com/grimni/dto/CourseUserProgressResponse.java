package com.grimni.dto;

import java.time.LocalDateTime;

import com.grimni.domain.CourseUserProgress;

public record CourseUserProgressResponse(
    Long courseId,
    Long userId,
    Boolean isCompleted,
    LocalDateTime lastUpdated,
    LocalDateTime createdAt
) {
    public static CourseUserProgressResponse fromEntity(CourseUserProgress progress) {
        return new CourseUserProgressResponse(
            progress.getCourse().getId(),
            progress.getUser().getId(),
            progress.getIsCompleted(),
            progress.getLastUpdated(),
            progress.getCreatedAt()
        );
    }
}
