package com.grimni.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "interval_rule")
public class IntervalRule extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interval_start", nullable = false)
    private Long intervalStart;

    @Column(name = "interval_repeat_time", nullable = false)
    private Long intervalRepeatTime;

    @OneToMany(mappedBy = "intervalRule")
    private List<Ccp> ccps = new ArrayList<>();

    @OneToMany(mappedBy = "intervalRule")
    private List<PrerequisiteRoutine> prerequisiteRoutines = new ArrayList<>();
}