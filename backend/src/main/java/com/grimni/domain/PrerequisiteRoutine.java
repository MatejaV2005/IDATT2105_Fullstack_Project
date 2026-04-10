package com.grimni.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "prerequisite_routine")
public class PrerequisiteRoutine extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "immediate_corrective_action", nullable = false, columnDefinition = "TEXT")
    private String immediateCorrectiveAction;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "prerequisite_description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_category_id")
    private PrerequisiteCategory prerequisiteCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interval_id")
    private IntervalRule intervalRule;

    @OneToMany(mappedBy = "routine")
    private List<RoutineUserBridge> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "routine")
    private List<PrerequisiteRoutineRecord> records = new ArrayList<>();
    
    public PrerequisiteRoutine() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImmediateCorrectiveAction() {
        return immediateCorrectiveAction;
    }

    public void setImmediateCorrectiveAction(String immediateCorrectiveAction) {
        this.immediateCorrectiveAction = immediateCorrectiveAction;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PrerequisiteCategory getPrerequisiteCategory() {
        return prerequisiteCategory;
    }

    public void setPrerequisiteCategory(PrerequisiteCategory prerequisiteCategory) {
        this.prerequisiteCategory = prerequisiteCategory;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public IntervalRule getIntervalRule() {
        return intervalRule;
    }

    public void setIntervalRule(IntervalRule intervalRule) {
        this.intervalRule = intervalRule;
    }

    public List<RoutineUserBridge> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<RoutineUserBridge> userRoles) {
        this.userRoles = userRoles;
    }

    public List<PrerequisiteRoutineRecord> getRecords() {
        return records;
    }

    public void setRecords(List<PrerequisiteRoutineRecord> records) {
        this.records = records;
    }

}
