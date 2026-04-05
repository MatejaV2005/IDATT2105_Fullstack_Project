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
}