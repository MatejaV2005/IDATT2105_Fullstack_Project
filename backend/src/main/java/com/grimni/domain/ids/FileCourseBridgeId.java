package com.grimni.domain.ids;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class FileCourseBridgeId implements Serializable {
    private Long courseId;
    private Long fileId;

    public FileCourseBridgeId() {}

    public FileCourseBridgeId(Long courseId, Long fileId) {
        this.courseId = courseId;
        this.fileId = fileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileCourseBridgeId that)) return false;
        return Objects.equals(courseId, that.courseId) && Objects.equals(fileId, that.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, fileId);
    }
}