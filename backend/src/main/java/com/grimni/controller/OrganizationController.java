package com.grimni.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.OrganizationResponse;
import com.grimni.dto.UpdateOrganizationRequest;
import com.grimni.domain.Organization;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.OrganizationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrganization(
        @Valid @RequestBody CreateOrganizationRequest request, 
        Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            Organization org = organizationService.createOrganization(request, principal.userId());
            return ResponseEntity.status(HttpStatus.CREATED).body(OrganizationResponse.fromEntity(org));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{orgId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrganization(@PathVariable Long orgId, Authentication authentication) {
        try {
            JwtUserPrinciple principle = (JwtUserPrinciple) authentication.getPrincipal();
            Organization org = organizationService.findOrganizationByIdAndUser(orgId, principle.userId());
            return ResponseEntity.ok(OrganizationResponse.fromEntity(org));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{orgId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateOrganization(@PathVariable Long orgId,
                                                @Valid @RequestBody UpdateOrganizationRequest request,
                                                Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            Organization org = organizationService.updateOrganization(orgId, request, principal.userId());
            return ResponseEntity.ok(OrganizationResponse.fromEntity(org));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}