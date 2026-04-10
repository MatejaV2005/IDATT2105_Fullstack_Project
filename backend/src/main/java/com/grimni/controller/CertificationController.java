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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for managing organizational certificates and professional credentials.
 * <p>
 * This controller provides administrative and user-level access to certificate records,
 * enforcing multi-tenancy through organization ID scoping and role-based access control (RBAC).
 * Operations include standard CRUD, partial updates via PATCH, and targeted retrieval by user.
 */
@Tag(name = "Certificates", description = "CRUD operations for organization certificates")
@RestController
@RequestMapping("/certificates")
public class CertificationController {

    private final CertificateService certificateService;

    public CertificationController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    /**
     * Registers a new certificate within the authenticated user's organization.
     * <p>
     * Access is restricted to users with administrative privileges (OWNER or MANAGER).
     *
     * @param request        The validated certificate creation details.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the newly created {@link CertificateResponse} and HTTP 201 status.
     */
    @Operation(summary = "Create certificate", description = "Creates a new certificate in the caller's organization")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createCertificate(
            @Valid @RequestBody CreateCertificateRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Certificate cert = certificateService.createCertificate(request, principal.orgId(), principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CertificateResponse.fromEntity(cert));
    }

    /**
     * Retrieves a list of all certificates associated with the caller's organization.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing a list of {@link CertificateResponse} objects.
     */
    @Operation(summary = "List organization certificates")
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

    /**
     * Retrieves the details of a specific certificate by its unique identifier.
     *
     * @param certId         The unique ID of the requested certificate.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the {@link CertificateResponse}.
     * @throws RuntimeException if the certificate does not exist or belongs to a different organization.
     */
    @Operation(summary = "Get certificate by ID")
    @GetMapping("/{certId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCertificate(@PathVariable Long certId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Certificate cert = certificateService.getCertificateById(certId, principal.orgId(), principal.userId());
        return ResponseEntity.ok(CertificateResponse.fromEntity(cert));
    }

    /**
     * Retrieves all certificates assigned to a specific user within the organization.
     *
     * @param targetUserId   The ID of the user whose certificates are being queried.
     * @param authentication The security context of the requester.
     * @return {@link ResponseEntity} containing a list of {@link CertificateResponse} for the target user.
     */
    @Operation(summary = "List certificates for a user")
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

    /**
     * Performs a partial update on an existing certificate.
     *
     * @param certId         The unique ID of the certificate to modify.
     * @param request        The validated request containing fields to be updated.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated {@link CertificateResponse}.
     */
    @Operation(summary = "Update certificate", description = "Partially updates a certificate by ID")
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

    /**
     * Deletes a certificate record from the system.
     *
     * @param certId         The unique ID of the certificate to remove.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with HTTP 204 No Content status upon successful deletion.
     */
    @Operation(summary = "Delete certificate")
    @DeleteMapping("/{certId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long certId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        certificateService.deleteCertificate(certId, principal.orgId(), principal.userId());
        return ResponseEntity.noContent().build();
    }
}