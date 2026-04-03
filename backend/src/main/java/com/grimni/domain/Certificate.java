package com.grimni.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
public class Certificate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String certificateText;

    private Instant expirationTime;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonBackReference("user-certificates")
    private User user;

    @ManyToOne
    @JoinColumn(name = "courseId")
    @JsonBackReference("course-certificates")
    private Course course;

    public Long getId() {
        return id;
    }

    public String getCertificateText() {
        return certificateText;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public User getUser() {
        return user;
    }

    public Course getCourse() {
        return course;
    }
}
