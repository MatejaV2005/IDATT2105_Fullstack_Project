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
    
    public Organization() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrgAddress() {
        return orgAddress;
    }

    public void setOrgAddress(String orgAddress) {
        this.orgAddress = orgAddress;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Boolean getAlcoholEnabled() {
        return alcoholEnabled;
    }

    public void setAlcoholEnabled(Boolean alcoholEnabled) {
        this.alcoholEnabled = alcoholEnabled;
    }

    public Boolean getFoodEnabled() {
        return foodEnabled;
    }

    public void setFoodEnabled(Boolean foodEnabled) {
        this.foodEnabled = foodEnabled;
    }

    public Integer getOrgNumber() {
        return orgNumber;
    }

    public void setOrgNumber(Integer orgNumber) {
        this.orgNumber = orgNumber;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
    }

    public List<OrgUserBridge> getMembers() {
        return members;
    }

    public void setMembers(List<OrgUserBridge> members) {
        this.members = members;
    }

    public List<OrgDangerAnalysisCollaborator> getDangerAnalysisCollaborators() {
        return dangerAnalysisCollaborators;
    }

    public void setDangerAnalysisCollaborators(List<OrgDangerAnalysisCollaborator> dangerAnalysisCollaborators) {
        this.dangerAnalysisCollaborators = dangerAnalysisCollaborators;
    }

    public List<ProductCategory> getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(List<ProductCategory> productCategories) {
        this.productCategories = productCategories;
    }

    public List<FileObject> getFiles() {
        return files;
    }

    public void setFiles(List<FileObject> files) {
        this.files = files;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<MappingPoint> getMappingPoints() {
        return mappingPoints;
    }

    public void setMappingPoints(List<MappingPoint> mappingPoints) {
        this.mappingPoints = mappingPoints;
    }

    public List<Ccp> getCcps() {
        return ccps;
    }

    public void setCcps(List<Ccp> ccps) {
        this.ccps = ccps;
    }

    public List<PrerequisiteCategory> getPrerequisiteCategories() {
        return prerequisiteCategories;
    }

    public void setPrerequisiteCategories(List<PrerequisiteCategory> prerequisiteCategories) {
        this.prerequisiteCategories = prerequisiteCategories;
    }

    public List<PrerequisiteRoutine> getPrerequisiteRoutines() {
        return prerequisiteRoutines;
    }

    public void setPrerequisiteRoutines(List<PrerequisiteRoutine> prerequisiteRoutines) {
        this.prerequisiteRoutines = prerequisiteRoutines;
    }

    public List<CcpRecord> getCcpRecords() {
        return ccpRecords;
    }

    public void setCcpRecords(List<CcpRecord> ccpRecords) {
        this.ccpRecords = ccpRecords;
    }

    public List<PrerequisiteRoutineRecord> getPrerequisiteRoutineRecords() {
        return prerequisiteRoutineRecords;
    }

    public void setPrerequisiteRoutineRecords(List<PrerequisiteRoutineRecord> prerequisiteRoutineRecords) {
        this.prerequisiteRoutineRecords = prerequisiteRoutineRecords;
    }

    public List<Deviation> getDeviations() {
        return deviations;
    }

    public void setDeviations(List<Deviation> deviations) {
        this.deviations = deviations;
    }

}