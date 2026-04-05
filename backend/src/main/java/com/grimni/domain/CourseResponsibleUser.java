package com.grimni.domain;

import com.grimni.domain.ids.CourseUserProgressId;
import jakarta.persistence.*;

@Entity
@Table(name = "user_course_bridge_responsible")
public class CourseResponsibleUser extends CreatedAtEntity {

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
    
    public CourseResponsibleUser() {
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

}