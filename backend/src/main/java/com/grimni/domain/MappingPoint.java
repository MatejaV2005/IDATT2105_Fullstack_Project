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
}