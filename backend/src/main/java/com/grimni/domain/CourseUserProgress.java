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
    
    public CourseUserProgress() {
    }

    public CourseUserProgressId getId() {
        return id;
    }

    public void setId(CourseUserProgressId id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}