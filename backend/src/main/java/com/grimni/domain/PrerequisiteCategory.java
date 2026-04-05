package com.grimni.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "prerequisite_category")
public class PrerequisiteCategory extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false, columnDefinition = "TEXT")
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @OneToMany(mappedBy = "prerequisiteCategory")
    private List<PrerequisiteStandard> standards = new ArrayList<>();

    @OneToMany(mappedBy = "prerequisiteCategory")
    private List<PrerequisiteRoutine> routines = new ArrayList<>();
    
    public PrerequisiteCategory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<PrerequisiteStandard> getStandards() {
        return standards;
    }

    public void setStandards(List<PrerequisiteStandard> standards) {
        this.standards = standards;
    }

    public List<PrerequisiteRoutine> getRoutines() {
        return routines;
    }

    public void setRoutines(List<PrerequisiteRoutine> routines) {
        this.routines = routines;
    }

}