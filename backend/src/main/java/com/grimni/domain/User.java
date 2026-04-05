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
}