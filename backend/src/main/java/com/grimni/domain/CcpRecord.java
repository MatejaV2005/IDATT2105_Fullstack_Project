package com.grimni.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.grimni.domain.enums.VerificationStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "ccp_record")
public class CcpRecord extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ccp_id")
    private Ccp ccp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_verifier")
    private User lastVerifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.WAITING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "measured_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal measuredValue;

    @Column(name = "critical_min", nullable = false, precision = 10, scale = 2)
    private BigDecimal criticalMin;

    @Column(name = "critical_max", nullable = false, precision = 10, scale = 2)
    private BigDecimal criticalMax;

    @Column(name = "unit", columnDefinition = "TEXT")
    private String unit;

    @Column(name = "ccp_name", nullable = false, columnDefinition = "TEXT")
    private String ccpName;
    
    public CcpRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Ccp getCcp() {
        return ccp;
    }

    public void setCcp(Ccp ccp) {
        this.ccp = ccp;
    }

    public User getLastVerifier() {
        return lastVerifier;
    }

    public void setLastVerifier(User lastVerifier) {
        this.lastVerifier = lastVerifier;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public User getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(User performedBy) {
        this.performedBy = performedBy;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BigDecimal getMeasuredValue() {
        return measuredValue;
    }

    public void setMeasuredValue(BigDecimal measuredValue) {
        this.measuredValue = measuredValue;
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

}