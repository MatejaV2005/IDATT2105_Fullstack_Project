package com.grimni.domain;

import java.time.LocalDateTime;

import com.grimni.domain.enums.DeviationCategory;
import com.grimni.domain.enums.ReviewStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "deviation")
public class Deviation extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ccp_record_id")
    private CcpRecord ccpRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_record_id")
    private PrerequisiteRoutineRecord routineRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private DeviationCategory category = DeviationCategory.OTHER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.OPEN;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "what_went_wrong", nullable = false, columnDefinition = "TEXT")
    private String whatWentWrong;

    @Column(name = "immediate_action_taken", nullable = false, columnDefinition = "TEXT")
    private String immediateActionTaken;

    @Column(name = "potential_cause", nullable = false, columnDefinition = "TEXT")
    private String potentialCause;

    @Column(name = "potential_preventative_measure", nullable = false, columnDefinition = "TEXT")
    private String potentialPreventativeMeasure;

    @Column(name = "preventative_measure_actually_taken", nullable = false, columnDefinition = "TEXT")
    private String preventativeMeasureActuallyTaken;

    public Deviation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CcpRecord getCcpRecord() {
        return ccpRecord;
    }

    public void setCcpRecord(CcpRecord ccpRecord) {
        this.ccpRecord = ccpRecord;
    }

    public PrerequisiteRoutineRecord getRoutineRecord() {
        return routineRecord;
    }

    public void setRoutineRecord(PrerequisiteRoutineRecord routineRecord) {
        this.routineRecord = routineRecord;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }

    public DeviationCategory getCategory() {
        return category;
    }

    public void setCategory(DeviationCategory category) {
        this.category = category;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getWhatWentWrong() {
        return whatWentWrong;
    }

    public void setWhatWentWrong(String whatWentWrong) {
        this.whatWentWrong = whatWentWrong;
    }

    public String getImmediateActionTaken() {
        return immediateActionTaken;
    }

    public void setImmediateActionTaken(String immediateActionTaken) {
        this.immediateActionTaken = immediateActionTaken;
    }

    public String getPotentialCause() {
        return potentialCause;
    }

    public void setPotentialCause(String potentialCause) {
        this.potentialCause = potentialCause;
    }

    public String getPotentialPreventativeMeasure() {
        return potentialPreventativeMeasure;
    }

    public void setPotentialPreventativeMeasure(String potentialPreventativeMeasure) {
        this.potentialPreventativeMeasure = potentialPreventativeMeasure;
    }

    public String getPreventativeMeasureActuallyTaken() {
        return preventativeMeasureActuallyTaken;
    }

    public void setPreventativeMeasureActuallyTaken(String preventativeMeasureActuallyTaken) {
        this.preventativeMeasureActuallyTaken = preventativeMeasureActuallyTaken;
    }
}