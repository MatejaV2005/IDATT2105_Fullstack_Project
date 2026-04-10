package com.grimni.dto;

import java.time.LocalDateTime;

import com.grimni.domain.CourseLink;

public record CourseLinkResponse(
    Long id,
    Long courseId,
    String link,
    LocalDateTime createdAt
) {
    public static CourseLinkResponse fromEntity(CourseLink courseLink) {
        return new CourseLinkResponse(
            courseLink.getId(),
            courseLink.getCourse().getId(),
            courseLink.getLink(),
            courseLink.getCreatedAt()
        );
    }
}
