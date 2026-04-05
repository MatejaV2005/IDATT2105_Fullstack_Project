package com.grimni.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "ccp")
public class Ccp extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "how", nullable = false, columnDefinition = "TEXT")
    private String how;

    @Column(name = "equipment", nullable = false, columnDefinition = "TEXT")
    private String equipment;

    @Column(name = "instructions_and_calibration", nullable = false, columnDefinition = "TEXT")
    private String instructionsAndCalibration;

    @Column(name = "immediate_corrective_action", nullable = false, columnDefinition = "TEXT")
    private String immediateCorrectiveAction;

    @Column(name = "critical_min", nullable = false, precision = 10, scale = 2)
    private BigDecimal criticalMin;

    @Column(name = "critical_max", nullable = false, precision = 10, scale = 2)
    private BigDecimal criticalMax;

    @Column(name = "unit", columnDefinition = "TEXT")
    private String unit;

    @Column(name = "ccp_name", nullable = false, columnDefinition = "TEXT")
    private String ccpName;

    @Column(name = "monitored_description", columnDefinition = "TEXT")
    private String monitoredDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interval_id")
    private IntervalRule intervalRule;

    @OneToMany(mappedBy = "ccp")
    private List<CcpUserBridge> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "ccp")
    private List<CcpCorrectiveMeasure> correctiveMeasures = new ArrayList<>();

    @OneToMany(mappedBy = "ccp")
    private List<CcpRecord> records = new ArrayList<>();

    public Ccp() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHow() {
        return how;
    }

    public void setHow(String how) {
        this.how = how;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getInstructionsAndCalibration() {
        return instructionsAndCalibration;
    }

    public void setInstructionsAndCalibration(String instructionsAndCalibration) {
        this.instructionsAndCalibration = instructionsAndCalibration;
    }

    public String getImmediateCorrectiveAction() {
        return immediateCorrectiveAction;
    }

    public void setImmediateCorrectiveAction(String immediateCorrectiveAction) {
        this.immediateCorrectiveAction = immediateCorrectiveAction;
    }

    public BigDecimal getCriticalMin() {
        return criticalMin;
    }

    public void setCriticalMin(BigDecimal criticalMin) {
        this.criticalMin = criticalMin;
    }

    public BigDecimal getCriticalMax() {
        return criticalMax;
    }

    public void setCriticalMax(BigDecimal criticalMax) {
        this.criticalMax = criticalMax;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCcpName() {
        return ccpName;
    }

    public void setCcpName(String ccpName) {
        this.ccpName = ccpName;
    }

    public String getMonitoredDescription() {
        return monitoredDescription;
    }

    public void setMonitoredDescription(String monitoredDescription) {
        this.monitoredDescription = monitoredDescription;
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

    public List<CcpUserBridge> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<CcpUserBridge> userRoles) {
        this.userRoles = userRoles;
    }

    public List<CcpCorrectiveMeasure> getCorrectiveMeasures() {
        return correctiveMeasures;
    }

    public void setCorrectiveMeasures(List<CcpCorrectiveMeasure> correctiveMeasures) {
        this.correctiveMeasures = correctiveMeasures;
    }

    public List<CcpRecord> getRecords() {
        return records;
    }

    public void setRecords(List<CcpRecord> records) {
        this.records = records;
    }
}
