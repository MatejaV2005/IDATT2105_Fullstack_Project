package com.grimni.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.domain.User;
import com.grimni.dto.CertificateResponse;
import com.grimni.dto.OrganizationResponse;
import com.grimni.dto.UpdateUserRequest;
import com.grimni.dto.UserResponse;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.CertificateService;
import com.grimni.service.OrganizationService;
import com.grimni.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/me")
public class MeController {

    private final OrganizationService organizationService;
    private final CertificateService certificateService;
    private final UserService userService;

    public MeController(OrganizationService organizationService, CertificateService certificateService, UserService userService) {
        this.organizationService = organizationService;
        this.certificateService = certificateService;
        this.userService = userService;
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
}
