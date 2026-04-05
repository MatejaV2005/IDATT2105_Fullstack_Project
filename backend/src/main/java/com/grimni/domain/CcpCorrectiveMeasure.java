package com.grimni.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ccp_corrective_measure")
public class CcpCorrectiveMeasure extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id")
    private ProductCategory productCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ccp_id")
    private Ccp ccp;

    @Column(name = "measure_description", nullable = false, columnDefinition = "TEXT")
    private String measureDescription;
    
    public CcpCorrectiveMeasure() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

    public Ccp getCcp() {
        return ccp;
    }

    public void setCcp(Ccp ccp) {
        this.ccp = ccp;
    }

    public String getMeasureDescription() {
        return measureDescription;
    }

    public void setMeasureDescription(String measureDescription) {
        this.measureDescription = measureDescription;
    }

}