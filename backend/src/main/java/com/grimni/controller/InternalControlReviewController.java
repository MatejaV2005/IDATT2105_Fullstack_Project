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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal-control-reviews")
public class InternalControlReviewController {

    private final InternalControlReviewService internalControlReviewService;

    public InternalControlReviewController(InternalControlReviewService internalControlReviewService) {
        this.internalControlReviewService = internalControlReviewService;
    }

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
