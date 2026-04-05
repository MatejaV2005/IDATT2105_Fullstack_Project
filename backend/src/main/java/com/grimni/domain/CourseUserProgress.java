package com.grimni.domain;

import java.time.LocalDateTime;

import com.grimni.domain.ids.CourseUserProgressId;
import jakarta.persistence.*;

@Entity
@Table(name = "course_user_bridge_progress")
public class CourseUserProgress extends CreatedAtEntity {

    @EmbeddedId
    private CourseUserProgressId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "last_updated", nullable = false, insertable = false, updatable = false)
    private LocalDateTime lastUpdated;
}