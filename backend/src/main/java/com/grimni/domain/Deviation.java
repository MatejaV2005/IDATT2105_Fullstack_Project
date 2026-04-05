package com.grimni.domain;

import java.time.LocalDateTime;

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
}