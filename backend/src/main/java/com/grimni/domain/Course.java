package com.grimni.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    @ManyToOne
    @JoinColumn(name = "orgId")
    private Organization organization;

    private String title;

    private String description;

    @OneToMany(mappedBy = "course")
    @JsonManagedReference("course-certificates") 
    private List<Certificate> certificates;

    public Long getCourseId() {
        return courseId;
    }
    public Organization getOrganization() {
        return organization;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public List<Certificate> getCertificates() {
        return certificates;
    }
}
