package com.grimni.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.dto.AddUserToOrgRequest;
import com.grimni.dto.RemoveUserFromOrgRequest;
import com.grimni.dto.CollaboratorResponse;
import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.OrganizationResponse;
import com.grimni.dto.UpdateOrganizationRequest;
import com.grimni.domain.Organization;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.OrganizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Manages organizations and their members.
 * Supports creating/updating organizations, listing users, adding/removing members,
 * and retrieving danger-analysis collaborators.
 */
@Tag(name = "Organizations", description = "Organization CRUD and member management")
@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /** Creates a new organization with the caller as OWNER. */
    @Operation(summary = "Create organization")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrganization(
        @Valid @RequestBody CreateOrganizationRequest request,
        Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Organization org = organizationService.createOrganization(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrganizationResponse.fromEntity(org));
    }

    /** Returns the caller's active organization. */
    @Operation(summary = "Get organization")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrganization(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Organization org = organizationService.findOrganizationByIdAndUser(principal.orgId(), principal.userId());
        return ResponseEntity.ok(OrganizationResponse.fromEntity(org));
    }

    /** Partially updates the caller's organization. */
    @Operation(summary = "Update organization")
    @PatchMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateOrganization(
                                                @Valid @RequestBody UpdateOrganizationRequest request,
                                                Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Organization org = organizationService.updateOrganization(principal.orgId(), request, principal.userId());
        return ResponseEntity.ok(OrganizationResponse.fromEntity(org));
    }

    /** Returns all users in the caller's organization. */
    @Operation(summary = "List organization users")
    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllUsersInOrg(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(organizationService.getAllUsersInOrg(principal.orgId()));
    }

    /** Adds a user to the organization with a specified role. OWNER only. */
    @Operation(summary = "Add user to organization")
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<?> addUserToOrg(
            @Valid @RequestBody AddUserToOrgRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                organizationService.addUserToOrg(request.userId(), request.role(), principal.orgId()));
    }

    /** Returns users eligible to collaborate on danger analysis. */
    @Operation(summary = "List danger-analysis collaborators")
    @GetMapping("/danger-analysis-collaborators")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> getDangerAnalysisCollaborators(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();

        List<CollaboratorResponse> response = organizationService.getDangerAnalysisCollaboratorsForOrg(principal.orgId());
        return ResponseEntity.ok(response);
    }

    /** Removes a user from the organization. Cannot remove an OWNER. */
    @Operation(summary = "Remove user from organization")
    @DeleteMapping("/users")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<?> deleteUserFromOrg(
            @Valid @RequestBody RemoveUserFromOrgRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        organizationService.removeUserFromOrg(request.userId(), principal.orgId());
        return ResponseEntity.ok().build();
    }
}
