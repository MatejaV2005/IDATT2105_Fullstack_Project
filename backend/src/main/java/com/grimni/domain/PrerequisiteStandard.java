package com.grimni.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "prerequisite_standard")
public class PrerequisiteStandard extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "standard_name", nullable = false, columnDefinition = "TEXT")
    private String standardName;

    @Column(name = "standard_description", nullable = false, columnDefinition = "TEXT")
    private String standardDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_category_id")
    private PrerequisiteCategory prerequisiteCategory;
    
    public PrerequisiteStandard() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    public String getStandardDescription() {
        return standardDescription;
    }

    public void setStandardDescription(String standardDescription) {
        this.standardDescription = standardDescription;
    }

    public PrerequisiteCategory getPrerequisiteCategory() {
        return prerequisiteCategory;
    }

    public void setPrerequisiteCategory(PrerequisiteCategory prerequisiteCategory) {
        this.prerequisiteCategory = prerequisiteCategory;
    }

}