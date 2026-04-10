package com.grimni.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.dto.CreateDeviationRequest;
import com.grimni.dto.DeviationResponse;
import com.grimni.dto.ResolveDeviationRequest;
import com.grimni.domain.Deviation;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.DeviationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing organizational deviations (avvik) and incident reporting.
 * <p>
 * This controller facilitates the quality assurance workflow by allowing users to report 
 * non-conformities, monitor pending reviews, and record resolution measures. It serves 
 * as a critical component for regulatory compliance and continuous process improvement.
 * </p>
 */
@Tag(name = "Deviations", description = "Report, list, and resolve deviations")
@RestController
@RequestMapping("/deviations")
public class DeviationController {

    private final DeviationService deviationService;

    public DeviationController(DeviationService deviationService) {
        this.deviationService = deviationService;
    }

    /**
     * Records a new deviation incident in the system.
     * <p>
     * Open to all authenticated users to encourage transparent reporting of non-conformities.
     *
     * @param request        The validated data transfer object containing incident details.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the created {@link DeviationResponse}.
     */
    @Operation(summary = "Report deviation", description = "Creates a new deviation record")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeviationResponse> createDeviation(
            @Valid @RequestBody CreateDeviationRequest request,
            Authentication authentication) {

        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Long userId = principal.userId();

        Deviation deviation = deviationService.createDeviation(request, userId);
        DeviationResponse response = DeviationResponse.fromEntity(deviation);
        return ResponseEntity.ok(response);
    }
      
    /**
     * Retrieves the count of active deviations currently awaiting administrative review.
     * <p>
     * The returned count is filtered based on the caller's organizational scope and role permissions.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the count of pending deviations as a {@link Long}.
     */
    @Operation(summary = "Get deviation review count")
    @GetMapping("/deviation-review-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getDeviationReviewCount(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        long count = deviationService.getDeviationReviewCount(principal.userId(), principal.orgId(), principal.role());
        return ResponseEntity.ok(count);
    }

    /**
     * Retrieves a comprehensive list of all deviations accessible to the caller.
     *
     * @param authentication The security context of the authenticated user.
     * @return {@link ResponseEntity} containing a list of {@link DeviationResponse} objects.
     */
    @Operation(summary = "List all deviations")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DeviationResponse>> getAllDeviations(Authentication authentication) {
        List<Deviation> deviations = deviationService.getAllDeviations();
        List<DeviationResponse> response = deviations.stream()
                .map(DeviationResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Resolves an existing deviation by documenting the corrective and preventative actions taken.
     * <p>
     * Closing a deviation transitions its status and archives the resolution for audit purposes.
     *
     * @param id             The unique identifier of the deviation to resolve.
     * @param request        The DTO containing the description of the measures actually implemented.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated {@link DeviationResponse}.
     */
    @Operation(summary = "Resolve deviation", description = "Closes a deviation with the measure taken")
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeviationResponse> resolveDeviation(
            @PathVariable Long id,
            @Valid @RequestBody ResolveDeviationRequest request,
            Authentication authentication) {

        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Long userId = principal.userId();

        Deviation resolvedDeviation = deviationService.resolveDeviation(id, userId, request.preventativeMeasureActuallyTaken());
        DeviationResponse response = DeviationResponse.fromEntity(resolvedDeviation);
        return ResponseEntity.ok(response);
    }
}