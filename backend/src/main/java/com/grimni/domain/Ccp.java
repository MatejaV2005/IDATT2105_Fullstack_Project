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
}