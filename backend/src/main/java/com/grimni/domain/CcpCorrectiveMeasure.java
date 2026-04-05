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
}