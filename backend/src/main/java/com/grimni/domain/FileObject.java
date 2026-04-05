package com.grimni.domain;

import java.util.ArrayList;
import java.util.List;

import com.grimni.domain.enums.AccessLevel;
import jakarta.persistence.*;

@Entity
@Table(name = "file_object")
public class FileObject extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "file_name", nullable = false, columnDefinition = "TEXT")
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(name = "object_key", nullable = false, columnDefinition = "TEXT")
    private String objectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "create_access", nullable = false)
    private AccessLevel createAccess;

    @Enumerated(EnumType.STRING)
    @Column(name = "read_access", nullable = false)
    private AccessLevel readAccess;

    @Enumerated(EnumType.STRING)
    @Column(name = "delete_access", nullable = false)
    private AccessLevel deleteAccess;

    @OneToMany(mappedBy = "file")
    private List<FileCourseBridge> linkedCourses = new ArrayList<>();
    
    public FileObject() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public AccessLevel getCreateAccess() {
        return createAccess;
    }

    public void setCreateAccess(AccessLevel createAccess) {
        this.createAccess = createAccess;
    }

    public AccessLevel getReadAccess() {
        return readAccess;
    }

    public void setReadAccess(AccessLevel readAccess) {
        this.readAccess = readAccess;
    }

    public AccessLevel getDeleteAccess() {
        return deleteAccess;
    }

    public void setDeleteAccess(AccessLevel deleteAccess) {
        this.deleteAccess = deleteAccess;
    }

    public List<FileCourseBridge> getLinkedCourses() {
        return linkedCourses;
    }

    public void setLinkedCourses(List<FileCourseBridge> linkedCourses) {
        this.linkedCourses = linkedCourses;
    }

}