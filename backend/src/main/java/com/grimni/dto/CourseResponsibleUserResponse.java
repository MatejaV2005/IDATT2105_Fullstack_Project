package com.grimni.dto;

import java.time.LocalDateTime;

import com.grimni.domain.CourseResponsibleUser;

public record CourseResponsibleUserResponse(
    Long courseId,
    Long userId,
    LocalDateTime createdAt
) {
    public static CourseResponsibleUserResponse fromEntity(CourseResponsibleUser responsible) {
        return new CourseResponsibleUserResponse(
            responsible.getCourse().getId(),
            responsible.getUser().getId(),
            responsible.getCreatedAt()
        );
    }
}
