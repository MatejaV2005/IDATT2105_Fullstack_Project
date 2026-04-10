package com.grimni.service;

import org.springframework.stereotype.Service;

import com.grimni.domain.CcpRecord;
import com.grimni.domain.Organization;
import com.grimni.domain.Deviation;
import com.grimni.domain.User;
import com.grimni.repository.DeviationRepository;
import com.grimni.repository.UserRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.CcpRecordRepository;

import java.util.List;

import com.grimni.dto.CreateDeviationRequest;
import com.grimni.domain.enums.ReviewStatus;

/**
 * Service class for managing organizational deviations (avvik) and non-conformity reports.
 * <p>
 * This service handles the documentation of incidents where standards were not met,
 * facilitating the lifecycle from initial reporting to administrative resolution.
 * It integrates with the CCP monitoring system by allowing deviations to be linked 
 * directly to specific measurement failures.
 */
@Service
public class DeviationService {

    private final DeviationRepository deviationRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final CcpRecordRepository ccpRecordRepository;

    public DeviationService(DeviationRepository deviationRepository, UserRepository userRepository,
                            OrganizationRepository organizationRepository, CcpRecordRepository ccpRecordRepository) {
        this.deviationRepository = deviationRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.ccpRecordRepository = ccpRecordRepository;
    }

    /**
     * Registers a new deviation report in the system.
     * <p>
     * Initializes the incident with an {@link ReviewStatus#OPEN} status and establishes 
     * links to the reporter and their organization. If the deviation was triggered 
     * by a failed CCP check, it links the corresponding {@link CcpRecord}.
     *
     * @param request    the DTO containing incident details and organizational context.
     * @param reporterId the unique identifier of the user reporting the incident.
     * @return the persisted {@link Deviation} entity.
     * @throws RuntimeException if the reporter user or organization is not found.
     */
    public Deviation createDeviation(CreateDeviationRequest request, Long reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Deviation deviation = new Deviation();
        deviation.setOrganization(organization);
        deviation.setReportedBy(reporter);
        deviation.setCategory(request.category());
        deviation.setWhatWentWrong(request.whatWentWrong());
        deviation.setImmediateActionTaken(request.immediateActionTaken());
        deviation.setPotentialCause(request.potentialCause());
        deviation.setPotentialPreventativeMeasure(request.potentialPreventativeMeasure());
        deviation.setPreventativeMeasureActuallyTaken("");
        deviation.setReviewStatus(ReviewStatus.OPEN);

        if (request.ccpRecordId() != null) {
            CcpRecord ccpRecord = ccpRecordRepository.findById(request.ccpRecordId())
                    .orElseThrow(() -> new RuntimeException("CCP Record not found"));
            deviation.setCcpRecord(ccpRecord);
        }

        return deviationRepository.save(deviation);
    }

    /**
     * Retrieves the count of deviations currently awaiting review.
     * <p>
     * The result is filtered based on multi-tenant isolation and the user's role 
     * permissions (OWNER/MANAGER vs. standard user).
     *
     * @param userId the requesting user's ID.
     * @param orgId  the organization ID.
     * @param role   the security role of the requesting user.
     * @return count of open deviation reviews.
     */
    public long getDeviationReviewCount(Long userId, Long orgId, String role) {
        boolean isManagerOrOwner = "OWNER".equals(role) || "MANAGER".equals(role);
        return deviationRepository.countOpenDeviationReviews(orgId, userId, isManagerOrOwner);
    }

    /**
     * Retrieves a list of all deviations recorded in the system.
     *
     * @return a list of {@link Deviation} entities.
     */
    public List<Deviation> getAllDeviations() {
        return deviationRepository.findAll();
    }

    /**
     * Fetches a specific deviation by its unique identifier.
     *
     * @param id the ID of the deviation.
     * @return the {@link Deviation} entity.
     * @throws RuntimeException if the deviation is not found.
     */
    public Deviation getDeviationById(Long id) {
        return deviationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deviation not found"));
    }

    /**
     * Resolves an open deviation incident.
     * <p>
     * Closes the case by recording the final preventative measures implemented, 
     * the reviewer's identity, and the timestamp of the resolution.
     *
     * @param id                  the unique identifier of the deviation to resolve.
     * @param reviewerId          the ID of the user performing the review.
     * @param preventativeMeasure a detailed description of the corrective action taken.
     * @return the updated and resolved {@link Deviation} entity.
     * @throws RuntimeException if the deviation or reviewer user cannot be found.
     */
    public Deviation resolveDeviation(Long id, Long reviewerId, String preventativeMeasure) {
        Deviation deviation = getDeviationById(id);

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        deviation.setReviewStatus(com.grimni.domain.enums.ReviewStatus.CLOSED);
        deviation.setReviewedBy(reviewer);
        deviation.setReviewedAt(java.time.LocalDateTime.now());
        deviation.setPreventativeMeasureActuallyTaken(preventativeMeasure);

        return deviationRepository.save(deviation);
    }
}