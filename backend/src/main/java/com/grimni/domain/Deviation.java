package com.grimni.domain;

import jakarta.persistence.*;
import java.time.Instant;

import com.grimni.util.Domain;

@Entity
@Table(name = "deviations")
public class Deviation {
    
    public enum Severity {
        LOW,
        HIGH,
        CRITICAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deviationId;

    @ManyToOne
    @JoinColumn(name = "orgId")
    private Organization organization;

    private String description;
    private Severity severity;
    private String immediateReaction;
    @ManyToOne
    @JoinColumn(name = "userId")
    private User reportedBy;
    private Instant reportedAt;
    private String correctiveAction;
    @ManyToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User resolvedBy;
    private Instant resolvedAt;
    private Domain domain;

    public Long getDeviationId() {
        return deviationId;
    }
    public Organization getOrganization() {
        return organization;
    }
    public String getDescription() {
        return description;
    }
    public String getCorrectiveAction() {
        return correctiveAction;
    }
    public Domain getDomain() {
        return domain;
    }
    public String getImmediateReaction() {
        return immediateReaction;
    }
    public Instant getReportedAt() {
        return reportedAt;
    }
    public User getReportedBy() {
        return reportedBy;
    }
    public Severity getSeverity() {
        return severity;
    }
    public User getResolvedBy() {
        return resolvedBy;
    }
    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
