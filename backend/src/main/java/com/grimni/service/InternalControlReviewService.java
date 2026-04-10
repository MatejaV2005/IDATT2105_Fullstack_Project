package com.grimni.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.domain.InternalControlReview;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.User;
import com.grimni.dto.CreateInternalControlReviewRequest;
import com.grimni.repository.InternalControlReviewRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service class for managing Internal Control Reviews (internkontroll-gjennomganger).
 * <p>
 * This service facilitates the formal documentation of periodic system audits. 
 * These reviews are used by management to evaluate the effectiveness of the 
 * organization's internal control measures and ensure continuous compliance 
 * with food safety regulations.
 */
@Service
public class InternalControlReviewService {

    private final InternalControlReviewRepository internalControlReviewRepository;
    private final OrgUserBridgeRepository orgUserBridgeRepository;
    private final UserRepository userRepository;

    public InternalControlReviewService(
            InternalControlReviewRepository internalControlReviewRepository,
            OrgUserBridgeRepository orgUserBridgeRepository,
            UserRepository userRepository) {
        this.internalControlReviewRepository = internalControlReviewRepository;
        this.orgUserBridgeRepository = orgUserBridgeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all internal control reviews for a specific organization, ordered by recency.
     * <p>
     * Access is scoped to the organization ID and verified against the authenticated user's membership.
     *
     * @param authenticatedUserId the unique ID of the requesting user.
     * @param orgId the unique ID of the organization scope.
     * @return a list of {@link InternalControlReview} entities.
     * @throws EntityNotFoundException if the requesting user does not belong to the organization.
     */
    @Transactional(readOnly = true)
    public List<InternalControlReview> getReviews(Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        return internalControlReviewRepository.findByOrganization_IdOrderByCreatedAtDesc(orgId);
    }

    /**
     * Creates and persists a new internal control review record.
     * <p>
     * Validates the organizational membership of the reviewer and ensures that the 
     * audit summary content is present and non-empty.
     *
     * @param request the DTO containing the audit summary content.
     * @param authenticatedUserId the unique ID of the user performing the review.
     * @param orgId the unique ID of the organization scope.
     * @return the persisted {@link InternalControlReview} entity.
     * @throws EntityNotFoundException if the user or organization reference is invalid.
     * @throws IllegalArgumentException if the provided summary consists solely of whitespace.
     */
    @Transactional
    public InternalControlReview createReview(
            CreateInternalControlReviewRequest request,
            Long authenticatedUserId,
            Long orgId) {
        OrgUserBridge membership = ensureAuthenticatedMember(authenticatedUserId, orgId);
        User reviewer = userRepository.findById(authenticatedUserId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String trimmedSummary = request.summary().trim();
        if (trimmedSummary.isBlank()) {
            throw new IllegalArgumentException("Summary cannot be blank");
        }

        InternalControlReview review = new InternalControlReview();
        review.setOrganization(membership.getOrganization());
        review.setReviewedBy(reviewer);
        review.setSummary(trimmedSummary);
        return internalControlReviewRepository.save(review);
    }

    /**
     * Verifies that the authenticated user is a valid member of the target organization.
     *
     * @param authenticatedUserId the ID of the user to check.
     * @param orgId the ID of the organization to check against.
     * @return the {@link OrgUserBridge} representing the membership.
     * @throws EntityNotFoundException if no membership is found.
     */
    private OrgUserBridge ensureAuthenticatedMember(Long authenticatedUserId, Long orgId) {
        return orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, authenticatedUserId)
            .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    }
}