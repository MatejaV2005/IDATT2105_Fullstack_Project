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

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/deviations")
public class DeviationController {

    private final DeviationService deviationService;

    public DeviationController(DeviationService deviationService) {
        this.deviationService = deviationService;
    }

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
      
    @GetMapping("/deviation-review-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getDeviationReviewCount(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        long count = deviationService.getDeviationReviewCount(principal.userId(), principal.orgId(), principal.role());
        return ResponseEntity.ok(count);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DeviationResponse>> getAllDeviations(Authentication authentication) {
        List<Deviation> deviations = deviationService.getAllDeviations();
        List<DeviationResponse> response = deviations.stream()
                .map(DeviationResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

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
