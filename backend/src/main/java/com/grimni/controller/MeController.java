package com.grimni.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.dto.CertificateResponse;
import com.grimni.dto.OrganizationResponse;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.CertificateService;
import com.grimni.service.OrganizationService;

@RestController
@RequestMapping("/me")
public class MeController {

    private final OrganizationService organizationService;
    private final CertificateService certificateService;

    public MeController(OrganizationService organizationService, CertificateService certificateService) {
        this.organizationService = organizationService;
        this.certificateService = certificateService;
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
}
