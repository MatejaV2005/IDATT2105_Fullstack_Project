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

    @Column(name = "product_name", nullable = false, columnDefinition = "TEXT")
    private String productName;

    @Column(name = "product_description", nullable = false, columnDefinition = "TEXT")
    private String productDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flowchart_file_id")
    private FileObject flowchartFile;

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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public FileObject getFlowchartFile() {
        return flowchartFile;
    }

    public void setFlowchartFile(FileObject flowchartFile) {
        this.flowchartFile = flowchartFile;
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
