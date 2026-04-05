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
    
    public Course() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<CourseUserProgress> getUserProgresses() {
        return userProgresses;
    }

    public void setUserProgresses(List<CourseUserProgress> userProgresses) {
        this.userProgresses = userProgresses;
    }

    public List<CourseResponsibleUser> getResponsibleUsers() {
        return responsibleUsers;
    }

    public void setResponsibleUsers(List<CourseResponsibleUser> responsibleUsers) {
        this.responsibleUsers = responsibleUsers;
    }

    public List<CourseLink> getLinks() {
        return links;
    }

    public void setLinks(List<CourseLink> links) {
        this.links = links;
    }

    public List<FileCourseBridge> getFiles() {
        return files;
    }

    public void setFiles(List<FileCourseBridge> files) {
        this.files = files;
    }

}