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

    @Transactional(readOnly = true)
    public List<InternalControlReview> getReviews(Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        return internalControlReviewRepository.findByOrganization_IdOrderByCreatedAtDesc(orgId);
    }

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

    private OrgUserBridge ensureAuthenticatedMember(Long authenticatedUserId, Long orgId) {
        return orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, authenticatedUserId)
            .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    }
}
