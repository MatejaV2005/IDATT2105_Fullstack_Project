package com.grimni.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.domain.Certificate;
import com.grimni.dto.CertificateResponse;
import com.grimni.dto.CreateCertificateRequest;
import com.grimni.dto.UpdateCertificateRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.CertificateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/certificates")
public class CertificationController {

    private final CertificateService certificateService;

    public CertificationController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createCertificate(
            @Valid @RequestBody CreateCertificateRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Certificate cert = certificateService.createCertificate(request, principal.orgId(), principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CertificateResponse.fromEntity(cert));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> getOrgCertificates(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        List<CertificateResponse> certs = certificateService.getCertificatesForOrg(principal.orgId(), principal.userId())
                .stream()
                .map(CertificateResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(certs);
    }

    @GetMapping("/{certId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCertificate(@PathVariable Long certId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Certificate cert = certificateService.getCertificateById(certId, principal.orgId(), principal.userId());
        return ResponseEntity.ok(CertificateResponse.fromEntity(cert));
    }

    @GetMapping("/user/{targetUserId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> getUserCertificates(@PathVariable Long targetUserId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        List<CertificateResponse> certs = certificateService.getCertificatesForUserInOrg(targetUserId, principal.orgId(), principal.userId())
                .stream()
                .map(CertificateResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(certs);
    }

    @PatchMapping("/{certId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateCertificate(
            @PathVariable Long certId,
            @Valid @RequestBody UpdateCertificateRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Certificate cert = certificateService.updateCertificate(certId, request, principal.orgId(), principal.userId());
        return ResponseEntity.ok(CertificateResponse.fromEntity(cert));
    }

    @DeleteMapping("/{certId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long certId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        certificateService.deleteCertificate(certId, principal.orgId(), principal.userId());
        return ResponseEntity.noContent().build();
    }
}
