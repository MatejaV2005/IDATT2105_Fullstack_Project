package com.grimni.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "course_link")
public class CourseLink extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "link", nullable = false, columnDefinition = "TEXT")
    private String link;
}