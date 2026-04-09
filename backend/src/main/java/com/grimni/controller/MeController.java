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
import com.grimni.dto.OrganizationResponse;
import com.grimni.dto.UpdateUserRequest;
import com.grimni.dto.UserResponse;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.CcpLoggingService;
import com.grimni.service.CertificateService;
import com.grimni.service.DeviationService;
import com.grimni.service.OrganizationService;
import com.grimni.service.RoutineLoggingService;
import com.grimni.service.UserService;

import jakarta.validation.Valid;

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

    @GetMapping("/organizations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyOrganizations(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        List<OrganizationResponse> orgs = organizationService.findOrganizationsByUserId(principal.userId())
                .stream()
                .map(OrganizationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(orgs);
    }

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

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        User user = userService.findUserById(principal.userId());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMyProfile(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        User user = userService.updateUser(principal.userId(), request);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @GetMapping("/routines")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAssignedRoutines(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(
            routineLoggingService.getAssignedRoutines(principal.userId(), principal.orgId())
        );
    }

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

    @GetMapping("/ccps")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAssignedCcps(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(
            ccpLoggingService.getAssignedCcps(principal.userId(), principal.orgId())
        );
    }

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
