package com.grimni.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "organization")
public class Organization extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_address", columnDefinition = "TEXT")
    private String orgAddress;

    @Column(name = "org_name", nullable = false, columnDefinition = "TEXT")
    private String orgName;

    @Column(name = "alcohol_enabled", nullable = false)
    private Boolean alcoholEnabled = false;

    @Column(name = "food_enabled", nullable = false)
    private Boolean foodEnabled = false;

    @Column(name = "org_number", nullable = false)
    private Integer orgNumber;

    @OneToMany(mappedBy = "organization")
    private List<Todo> todos = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<OrgUserBridge> members = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<OrgDangerAnalysisCollaborator> dangerAnalysisCollaborators = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<ProductCategory> productCategories = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<FileObject> files = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<MappingPoint> mappingPoints = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<Ccp> ccps = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<PrerequisiteCategory> prerequisiteCategories = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<PrerequisiteRoutine> prerequisiteRoutines = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<CcpRecord> ccpRecords = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<PrerequisiteRoutineRecord> prerequisiteRoutineRecords = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<Deviation> deviations = new ArrayList<>();
}