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
}