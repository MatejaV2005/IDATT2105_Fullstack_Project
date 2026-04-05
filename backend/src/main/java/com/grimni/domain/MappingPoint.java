package com.grimni.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "mapping_point")
public class MappingPoint extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "challenges", columnDefinition = "TEXT")
    private String challenges;

    @Column(name = "measures", columnDefinition = "TEXT")
    private String measures;

    @Column(name = "law", length = 25)
    private String law;

    @Column(name = "severity_dots")
    private Short severityDots;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @OneToMany(mappedBy = "mappingPoint")
    private List<MappingPointResponsibleUser> responsibleUsers = new ArrayList<>();
    
    public MappingPoint() {
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

    public String getChallenges() {
        return challenges;
    }

    public void setChallenges(String challenges) {
        this.challenges = challenges;
    }

    public String getMeasures() {
        return measures;
    }

    public void setMeasures(String measures) {
        this.measures = measures;
    }

    public String getLaw() {
        return law;
    }

    public void setLaw(String law) {
        this.law = law;
    }

    public Short getSeverityDots() {
        return severityDots;
    }

    public void setSeverityDots(Short severityDots) {
        this.severityDots = severityDots;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<MappingPointResponsibleUser> getResponsibleUsers() {
        return responsibleUsers;
    }

    public void setResponsibleUsers(List<MappingPointResponsibleUser> responsibleUsers) {
        this.responsibleUsers = responsibleUsers;
    }

}