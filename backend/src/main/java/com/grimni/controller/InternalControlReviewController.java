package com.grimni.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.dto.CreateInternalControlReviewRequest;
import com.grimni.dto.InternalControlReviewResponse;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.InternalControlReviewService;

import jakarta.persistence.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for managing Internal Control Reviews (internkontroll-gjennomganger).
 * <p>
 * This controller handles the documentation of periodic system audits performed by 
 * management to verify organizational compliance with internal standards and external 
 * regulations. These reviews form a cornerstone of the Internal Control System (ICS).
 * </p>
 */
@Tag(name = "Internal Control Reviews", description = "Create and list internal control reviews")
@RestController
@RequestMapping("/internal-control-reviews")
public class InternalControlReviewController {

    private final InternalControlReviewService internalControlReviewService;

    public InternalControlReviewController(InternalControlReviewService internalControlReviewService) {
        this.internalControlReviewService = internalControlReviewService;
    }

    /**
     * Retrieves all historical internal control reviews for the authenticated user's organization.
     * <p>
     * Access is restricted to users with OWNER or MANAGER authority, as these records 
     * often contain sensitive compliance and audit data.
     * </p>
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing a list of {@link InternalControlReviewResponse} objects.
     */
    @Operation(summary = "List reviews")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<List<InternalControlReviewResponse>> getReviews(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        List<InternalControlReviewResponse> reviews = internalControlReviewService
            .getReviews(principal.userId(), principal.orgId())
            .stream()
            .map(InternalControlReviewResponse::fromEntity)
            .toList();
        return ResponseEntity.ok(reviews);
    }

    /**
     * Formalizes and persists a new internal control review.
     * <p>
     * This endpoint captures the outcome of a periodic compliance audit. It ensures 
     * that only high-level stakeholders (OWNER/MANAGER) can submit these records, 
     * providing an immutable audit trail for the organization's control system.
     * </p>
     *
     * @param request        The validated DTO containing the review findings and metadata.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with the created {@link InternalControlReviewResponse}.
     * @throws IllegalArgumentException if the request parameters are logically inconsistent.
     * @throws EntityNotFoundException  if associated resources (e.g., organization) are missing.
     */
    @Operation(summary = "Create review")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createReview(
            @Valid @RequestBody CreateInternalControlReviewRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.status(HttpStatus.CREATED).body(
                InternalControlReviewResponse.fromEntity(
                    internalControlReviewService.createReview(request, principal.userId(), principal.orgId())
                )
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }
}