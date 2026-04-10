package com.grimni.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.domain.User;
import com.grimni.dto.CreateCcpRecordRequest;
import com.grimni.dto.CreateMyDeviationRequest;
import com.grimni.dto.CertificateResponse;
import com.grimni.dto.MyOrganizationMembershipResponse;
import com.grimni.dto.UpdateUserRequest;
import com.grimni.dto.UserResponse;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.CcpLoggingService;
import com.grimni.service.CertificateService;
import com.grimni.service.DeviationService;
import com.grimni.service.OrganizationService;
import com.grimni.service.RoutineLoggingService;
import com.grimni.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller providing user-centric endpoints for the currently authenticated principal.
 * <p>
 * This controller serves as a "Personal Workspace" API, allowing users to:
 * <ul>
 * <li>Manage their personal profile and organizational memberships.</li>
 * <li>Access personal professional credentials and certificates.</li>
 * <li>Interact with assigned operational tasks, including Routines and Critical Control Points (CCPs).</li>
 * <li>Execute immediate compliance actions like logging measurements and reporting deviations.</li>
 * </ul>
 * All operations are implicitly scoped to the {@code userId} and {@code orgId} derived from the JWT.
 */
@Tag(name = "Me", description = "Current user's profile, assignments, and actions")
@RestController
@RequestMapping("/me")
public class MeController {

    private final OrganizationService organizationService;
    private final CertificateService certificateService;
    private final UserService userService;
    private final RoutineLoggingService routineLoggingService;
    private final CcpLoggingService ccpLoggingService;
    private final DeviationService deviationService;

    public MeController(
            OrganizationService organizationService,
            CertificateService certificateService,
            UserService userService,
            RoutineLoggingService routineLoggingService,
            CcpLoggingService ccpLoggingService,
            DeviationService deviationService) {
        this.organizationService = organizationService;
        this.certificateService = certificateService;
        this.userService = userService;
        this.routineLoggingService = routineLoggingService;
        this.ccpLoggingService = ccpLoggingService;
        this.deviationService = deviationService;
    }

    /**
     * Retrieves all organizations associated with the authenticated user.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing a list of {@link OrganizationResponse} objects.
     */
    @Operation(summary = "List my organizations")
    @GetMapping("/organizations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyOrganizations(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        List<MyOrganizationMembershipResponse> orgs = organizationService.getMyOrganizationMemberships(
            principal.userId(),
            principal.orgId()
        );
        return ResponseEntity.ok(orgs);
    }

    /**
     * Retrieves all professional certificates assigned to the authenticated user.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing a list of {@link CertificateResponse} objects.
     */
    @Operation(summary = "List my certificates")
    @GetMapping("/certificates")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyCertificates(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        List<CertificateResponse> certs = certificateService.getCertificatesForUser(principal.userId())
                .stream()
                .map(CertificateResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(certs);
    }

    /**
     * Retrieves the profile details of the currently authenticated user.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the {@link UserResponse}.
     */
    @Operation(summary = "Get my profile")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        User user = userService.findUserById(principal.userId());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * Updates the authenticated user's profile information.
     *
     * @param request        Validated update request containing the new profile data.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated {@link UserResponse}.
     */
    @Operation(summary = "Update my profile")
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMyProfile(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        User user = userService.updateUser(principal.userId(), request);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * Lists all routines assigned to the authenticated user within their current organization.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the list of assigned routines and their status.
     */
    @Operation(summary = "List my assigned routines")
    @GetMapping("/routines")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAssignedRoutines(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(
            routineLoggingService.getAssignedRoutines(principal.userId(), principal.orgId())
        );
    }

    /**
     * Logs the completion of a specific routine for the authenticated user.
     *
     * @param routineId      The unique identifier of the routine to mark as complete.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} confirming the routine completion record.
     */
    @Operation(summary = "Complete a routine")
    @PostMapping("/routines/{routineId}/records")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> completeRoutine(
            @PathVariable Long routineId,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(
            routineLoggingService.completeRoutine(routineId, principal.userId(), principal.orgId())
        );
    }

    /**
     * Lists all Critical Control Points (CCPs) assigned to the authenticated user.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the assigned CCPs.
     */
    @Operation(summary = "List my assigned CCPs")
    @GetMapping("/ccps")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAssignedCcps(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(
            ccpLoggingService.getAssignedCcps(principal.userId(), principal.orgId())
        );
    }

    /**
     * Records a new measurement or observation for a specific Critical Control Point.
     *
     * @param ccpId          The unique identifier of the CCP.
     * @param request        Validated data containing the measurement values.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the created CCP record.
     */
    @Operation(summary = "Log CCP record")
    @PostMapping("/ccps/{ccpId}/records")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createCcpRecord(
            @PathVariable Long ccpId,
            @Valid @RequestBody CreateCcpRecordRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(
            ccpLoggingService.createRecord(ccpId, request, principal.userId(), principal.orgId())
        );
    }

    /**
     * Reports a process deviation (avvik) initiated by the authenticated user.
     * <p>
     * The deviation is automatically associated with the user's active organization.
     *
     * @param request        The validated deviation report request.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the {@link com.grimni.dto.DeviationResponse}.
     */
    @Operation(summary = "Report deviation (as me)")
    @PostMapping("/deviations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createDeviation(
            @Valid @RequestBody CreateMyDeviationRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(
            com.grimni.dto.DeviationResponse.fromEntity(
                deviationService.createDeviation(
                    request.toCreateDeviationRequest(principal.orgId()),
                    principal.userId()
                )
            )
        );
    }
}