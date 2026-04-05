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
    
    public DangerRiskCombo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDanger() {
        return danger;
    }

    public void setDanger(String danger) {
        this.danger = danger;
    }

    public String getDangerCorrectiveMeasure() {
        return dangerCorrectiveMeasure;
    }

    public void setDangerCorrectiveMeasure(String dangerCorrectiveMeasure) {
        this.dangerCorrectiveMeasure = dangerCorrectiveMeasure;
    }

    public Integer getSeverityScore() {
        return severityScore;
    }

    public void setSeverityScore(Integer severityScore) {
        this.severityScore = severityScore;
    }

    public Integer getLikelihoodScore() {
        return likelihoodScore;
    }

    public void setLikelihoodScore(Integer likelihoodScore) {
        this.likelihoodScore = likelihoodScore;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

}