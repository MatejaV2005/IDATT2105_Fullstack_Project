package com.grimni.controller;

import java.util.List;

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

import com.grimni.dto.CollaboratorResponse;
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
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Organization org = organizationService.createOrganization(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrganizationResponse.fromEntity(org));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrganization(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Organization org = organizationService.findOrganizationByIdAndUser(principal.orgId(), principal.userId());
        return ResponseEntity.ok(OrganizationResponse.fromEntity(org));
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateOrganization(
                                                @Valid @RequestBody UpdateOrganizationRequest request,
                                                Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Organization org = organizationService.updateOrganization(principal.orgId(), request, principal.userId());
        return ResponseEntity.ok(OrganizationResponse.fromEntity(org));
    }

    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllUsersInOrg(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(organizationService.getAllUsersInOrg(principal.orgId()));
    }

    @GetMapping("/danger-analysis-collaborators")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> getDangerAnalysisCollaborators(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();

        List<CollaboratorResponse> response = organizationService.getDangerAnalysisCollaboratorsForOrg(principal.orgId());
        return ResponseEntity.ok(response);
    }
}
