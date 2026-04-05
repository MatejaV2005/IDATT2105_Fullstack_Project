package com.grimni.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Lob
    @Column(name = "salt", nullable = false)
    private byte[] salt;

    @Column(name = "legal_name", nullable = false, columnDefinition = "TEXT")
    private String legalName;

    @OneToMany(mappedBy = "user")
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTo")
    private List<Todo> assignedTodos = new ArrayList<>();

    @OneToMany(mappedBy = "uploadedBy")
    private List<FileObject> uploadedFiles = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<OrgUserBridge> organizations = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<OrgDangerAnalysisCollaborator> dangerAnalysisOrganizations = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<CourseUserProgress> courseProgresses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<CourseResponsibleUser> responsibleCourses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<MappingPointResponsibleUser> mappingPointResponsibilities = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<RoutineUserBridge> routineRoles = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<CcpUserBridge> ccpRoles = new ArrayList<>();

    @OneToMany(mappedBy = "lastVerifier")
    private List<CcpRecord> verifiedCcpRecords = new ArrayList<>();

    @OneToMany(mappedBy = "performedBy")
    private List<CcpRecord> performedCcpRecords = new ArrayList<>();

    @OneToMany(mappedBy = "performedBy")
    private List<PrerequisiteRoutineRecord> performedRoutineRecords = new ArrayList<>();

    @OneToMany(mappedBy = "lastVerifier")
    private List<PrerequisiteRoutineRecord> verifiedRoutineRecords = new ArrayList<>();

    @OneToMany(mappedBy = "reportedBy")
    private List<Deviation> reportedDeviations = new ArrayList<>();

    @OneToMany(mappedBy = "reviewedBy")
    private List<Deviation> reviewedDeviations = new ArrayList<>();



    

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public List<RefreshToken> getRefreshTokens() {
        return refreshTokens;
    }

    public void setRefreshTokens(List<RefreshToken> refreshTokens) {
        this.refreshTokens = refreshTokens;
    }

    public List<Todo> getAssignedTodos() {
        return assignedTodos;
    }

    public void setAssignedTodos(List<Todo> assignedTodos) {
        this.assignedTodos = assignedTodos;
    }

    public List<FileObject> getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(List<FileObject> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public List<OrgUserBridge> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrgUserBridge> organizations) {
        this.organizations = organizations;
    }

    public List<OrgDangerAnalysisCollaborator> getDangerAnalysisOrganizations() {
        return dangerAnalysisOrganizations;
    }

    public void setDangerAnalysisOrganizations(List<OrgDangerAnalysisCollaborator> dangerAnalysisOrganizations) {
        this.dangerAnalysisOrganizations = dangerAnalysisOrganizations;
    }

    public List<CourseUserProgress> getCourseProgresses() {
        return courseProgresses;
    }

    public void setCourseProgresses(List<CourseUserProgress> courseProgresses) {
        this.courseProgresses = courseProgresses;
    }

    public List<CourseResponsibleUser> getResponsibleCourses() {
        return responsibleCourses;
    }

    public void setResponsibleCourses(List<CourseResponsibleUser> responsibleCourses) {
        this.responsibleCourses = responsibleCourses;
    }

    public List<MappingPointResponsibleUser> getMappingPointResponsibilities() {
        return mappingPointResponsibilities;
    }

    public void setMappingPointResponsibilities(List<MappingPointResponsibleUser> mappingPointResponsibilities) {
        this.mappingPointResponsibilities = mappingPointResponsibilities;
    }

    public List<RoutineUserBridge> getRoutineRoles() {
        return routineRoles;
    }

    public void setRoutineRoles(List<RoutineUserBridge> routineRoles) {
        this.routineRoles = routineRoles;
    }

    public List<CcpUserBridge> getCcpRoles() {
        return ccpRoles;
    }

    public void setCcpRoles(List<CcpUserBridge> ccpRoles) {
        this.ccpRoles = ccpRoles;
    }

    public List<CcpRecord> getVerifiedCcpRecords() {
        return verifiedCcpRecords;
    }

    public void setVerifiedCcpRecords(List<CcpRecord> verifiedCcpRecords) {
        this.verifiedCcpRecords = verifiedCcpRecords;
    }

    public List<CcpRecord> getPerformedCcpRecords() {
        return performedCcpRecords;
    }

    public void setPerformedCcpRecords(List<CcpRecord> performedCcpRecords) {
        this.performedCcpRecords = performedCcpRecords;
    }

    public List<PrerequisiteRoutineRecord> getPerformedRoutineRecords() {
        return performedRoutineRecords;
    }

    public void setPerformedRoutineRecords(List<PrerequisiteRoutineRecord> performedRoutineRecords) {
        this.performedRoutineRecords = performedRoutineRecords;
    }

    public List<PrerequisiteRoutineRecord> getVerifiedRoutineRecords() {
        return verifiedRoutineRecords;
    }

    public void setVerifiedRoutineRecords(List<PrerequisiteRoutineRecord> verifiedRoutineRecords) {
        this.verifiedRoutineRecords = verifiedRoutineRecords;
    }

    public List<Deviation> getReportedDeviations() {
        return reportedDeviations;
    }

    public void setReportedDeviations(List<Deviation> reportedDeviations) {
        this.reportedDeviations = reportedDeviations;
    }

    public List<Deviation> getReviewedDeviations() {
        return reviewedDeviations;
    }

    public void setReviewedDeviations(List<Deviation> reviewedDeviations) {
        this.reviewedDeviations = reviewedDeviations;
    }
}