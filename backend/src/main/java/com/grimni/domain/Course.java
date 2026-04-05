package com.grimni.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "course")
public class Course extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "course_description", nullable = false, columnDefinition = "TEXT")
    private String courseDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @OneToMany(mappedBy = "course")
    private List<CourseUserProgress> userProgresses = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    private List<CourseResponsibleUser> responsibleUsers = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    private List<CourseLink> links = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    private List<FileCourseBridge> files = new ArrayList<>();
}