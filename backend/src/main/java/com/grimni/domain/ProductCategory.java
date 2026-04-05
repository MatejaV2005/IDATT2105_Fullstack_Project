package com.grimni.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "product_category")
public class ProductCategory extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_description", nullable = false, columnDefinition = "TEXT")
    private String productDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name = "flowchart", nullable = false, columnDefinition = "JSON")
    private String flowchart;

    @OneToMany(mappedBy = "productCategory")
    private List<DangerRiskCombo> dangerRiskCombos = new ArrayList<>();

    @OneToMany(mappedBy = "productCategory")
    private List<CcpCorrectiveMeasure> correctiveMeasures = new ArrayList<>();
    
    public ProductCategory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getFlowchart() {
        return flowchart;
    }

    public void setFlowchart(String flowchart) {
        this.flowchart = flowchart;
    }

    public List<DangerRiskCombo> getDangerRiskCombos() {
        return dangerRiskCombos;
    }

    public void setDangerRiskCombos(List<DangerRiskCombo> dangerRiskCombos) {
        this.dangerRiskCombos = dangerRiskCombos;
    }

    public List<CcpCorrectiveMeasure> getCorrectiveMeasures() {
        return correctiveMeasures;
    }

    public void setCorrectiveMeasures(List<CcpCorrectiveMeasure> correctiveMeasures) {
        this.correctiveMeasures = correctiveMeasures;
    }

}