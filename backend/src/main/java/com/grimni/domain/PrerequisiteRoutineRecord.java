package com.grimni.domain;

import java.time.LocalDateTime;

import com.grimni.domain.enums.ResultStatus;
import com.grimni.domain.enums.VerificationStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "prerequisite_routine_record")
public class PrerequisiteRoutineRecord extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private PrerequisiteRoutine routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false)
    private ResultStatus resultStatus;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_verifier")
    private User lastVerifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.WAITING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
}