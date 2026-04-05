package com.grimni.domain.ids;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class CourseUserProgressId implements Serializable {
    private Long courseId;
    private Long userId;

    public CourseUserProgressId() {}

    public CourseUserProgressId(Long courseId, Long userId) {
        this.courseId = courseId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseUserProgressId that)) return false;
        return Objects.equals(courseId, that.courseId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, userId);
    }
}