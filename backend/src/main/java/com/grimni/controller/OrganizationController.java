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
import com.grimni.dto.UpdateUserOrgRoleRequest;
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
 * REST controller for managing organizational entities and their administrative memberships.
 * <p>
 * This controller serves as the primary gateway for multi-tenant management, providing
 * capabilities for:
 * <ul>
 * <li>Organization lifecycle management (CRUD).</li>
 * <li>User membership and role-based access control (RBAC) within an organization.</li>
 * <li>Collaborator identification for specialized tasks such as HACCP danger analysis.</li>
 * </ul>
 * All operations are strictly validated against the authenticated user's scope and permissions.
 */
@Tag(name = "Organizations", description = "Organization CRUD and member management")
@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Initializes a new organization entity and assigns the creator as the primary OWNER.
     *
     * @param request        The validated data required to initialize the organization.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the created {@link OrganizationResponse}.
     */
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

    /**
     * Retrieves the details of the authenticated user's current active organization.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the {@link OrganizationResponse}.
     */
    @Operation(summary = "Get organization")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrganization(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Organization org = organizationService.findOrganizationByIdAndUser(principal.orgId(), principal.userId());
        return ResponseEntity.ok(OrganizationResponse.fromEntity(org));
    }

    /**
     * Performs a partial update on the current organization's configuration.
     * <p>Access is restricted to users with OWNER authority.</p>
     *
     * @param request        The validated request containing organization updates.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated {@link OrganizationResponse}.
     */
    @Operation(summary = "Update organization")
    @PatchMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<?> updateOrganization(
                                                @Valid @RequestBody UpdateOrganizationRequest request,
                                                Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Organization org = organizationService.updateOrganization(principal.orgId(), request, principal.userId());
        return ResponseEntity.ok(OrganizationResponse.fromEntity(org));
    }

    @Operation(summary = "Delete organization")
    @DeleteMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<?> deleteOrganization(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        organizationService.deleteOrganization(principal.orgId(), principal.userId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all users currently registered as members of the authenticated user's organization.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing a list of organizational members.
     */
    @Operation(summary = "List organization users")
    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllUsersInOrg(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(organizationService.getAllUsersInOrg(principal.orgId()));
    }

    @Operation(summary = "Get organization analysis data")
    @GetMapping("/analysis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrgAnalysis(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(organizationService.getOrgAnalysis(principal.orgId()));
    }

    @Operation(summary = "Get team overview")
    @GetMapping("/team-overview")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> getTeamOverview(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(organizationService.getTeamOverview(principal.orgId()));
    }

    @Operation(summary = "Get organization user directory")
    @GetMapping("/user-directory")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<?> getUserDirectory(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(organizationService.getUserDirectory(principal.orgId()));
    }

    /**
     * Grants a user membership to the organization with a specific assigned role.
     * <p>Strictly restricted to users with OWNER authority.</p>
     *
     * @param request        The request containing the target user ID and their intended role.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} confirming the user's addition with HTTP 201 status.
     */
    @Operation(summary = "Add user to organization")
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<?> addUserToOrg(
            @Valid @RequestBody AddUserToOrgRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                organizationService.addUserToOrg(request.userId(), request.role(), principal.orgId(), principal.userId()));
    }

    @Operation(summary = "Update user role in organization")
    @PatchMapping("/users")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<?> updateUserRoleInOrg(
            @Valid @RequestBody UpdateUserOrgRoleRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(
                organizationService.updateUserRoleInOrg(request.userId(), request.role(), principal.orgId(), principal.userId()));
    }

    /**
     * Retrieves a specialized list of members qualified to participate in danger-analysis workflows.
     * <p>Requires OWNER or MANAGER authority.</p>
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing a list of {@link CollaboratorResponse} objects.
     */
    @Operation(summary = "List danger-analysis collaborators")
    @GetMapping("/danger-analysis-collaborators")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> getDangerAnalysisCollaborators(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        List<CollaboratorResponse> response = organizationService.getDangerAnalysisCollaboratorsForOrg(principal.orgId());
        return ResponseEntity.ok(response);
    }

    /**
     * Revokes a user's membership from the organization.
     * <p>Strictly restricted to users with OWNER authority. 
     * Note: Protection is in place to prevent the removal of the primary OWNER.</p>
     *
     * @param request        The request specifying the user ID to be removed.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with HTTP 200 OK status on success.
     */
    @Operation(summary = "Remove user from organization")
    @DeleteMapping("/users")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<?> deleteUserFromOrg(
            @Valid @RequestBody RemoveUserFromOrgRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        organizationService.removeUserFromOrg(request.userId(), principal.orgId(), principal.userId());
        return ResponseEntity.ok().build();
    }
}
