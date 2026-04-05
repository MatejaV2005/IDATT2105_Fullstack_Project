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
    
    public IntervalRule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIntervalStart() {
        return intervalStart;
    }

    public void setIntervalStart(Long intervalStart) {
        this.intervalStart = intervalStart;
    }

    public Long getIntervalRepeatTime() {
        return intervalRepeatTime;
    }

    public void setIntervalRepeatTime(Long intervalRepeatTime) {
        this.intervalRepeatTime = intervalRepeatTime;
    }

    public List<Ccp> getCcps() {
        return ccps;
    }

    public void setCcps(List<Ccp> ccps) {
        this.ccps = ccps;
    }

    public List<PrerequisiteRoutine> getPrerequisiteRoutines() {
        return prerequisiteRoutines;
    }

    public void setPrerequisiteRoutines(List<PrerequisiteRoutine> prerequisiteRoutines) {
        this.prerequisiteRoutines = prerequisiteRoutines;
    }

}