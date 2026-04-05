package com.grimni.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "danger_risk_combo")
public class DangerRiskCombo extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "danger", nullable = false, columnDefinition = "TEXT")
    private String danger;

    @Column(name = "danger_corrective_measure", nullable = false, columnDefinition = "TEXT")
    private String dangerCorrectiveMeasure;

    @Column(name = "severity_score", nullable = false)
    private Integer severityScore;

    @Column(name = "likelihood_score", nullable = false)
    private Integer likelihoodScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id")
    private ProductCategory productCategory;
}