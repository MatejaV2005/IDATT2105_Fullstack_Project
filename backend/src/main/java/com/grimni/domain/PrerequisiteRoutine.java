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
}