package com.grimni.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.dto.CreateCcpCorrectiveMeasureRequest;
import com.grimni.dto.CreateCcpRequest;
import com.grimni.dto.ReplaceCcpAssignmentsRequest;
import com.grimni.dto.UpdateCcpCorrectiveMeasureRequest;
import com.grimni.dto.UpdateCcpRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.CcpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Manages Critical Control Points (CCPs) in the HACCP system.
 * Supports CRUD for CCPs, user assignments, corrective measures,
 * and verification logs/counts.
 */
@Tag(name = "Critical Control Points", description = "HACCP CCP management, assignments, and corrective measures")
@RestController
public class CcpController {

    private final CcpService ccpService;

    public CcpController(CcpService ccpService) {
        this.ccpService = ccpService;
    }

    /** Returns the number of CCP records pending verification. */
    @Operation(summary = "Get verification count")
    @GetMapping("/ccps/verification-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getVerificationCount(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        long count = ccpService.getVerificationCount(principal.userId(), principal.orgId(), principal.role());
        return ResponseEntity.ok(count);
    }

    /** Returns CCP verification logs visible to the caller. */
    @Operation(summary = "Get verification logs")
    @GetMapping("/ccps/logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getVerificationLogs(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(
            ccpService.getVerificationLogs(principal.userId(), principal.orgId(), principal.role())
        );
    }

    /** Returns all CCPs with full detail for the caller's organization. */
    @Operation(summary = "Get all CCP info")
    @GetMapping("/haccp/critical-control-points/get-all-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllInfo(Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                ccpService.getAllInfo(principal.userId(), principal.orgId())
            );
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }    

    /** Creates a new CCP. */
    @Operation(summary = "Create CCP")
    @PostMapping("/haccp/critical-control-points")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createCcp(
            @Valid @RequestBody CreateCcpRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ccpService.createCcp(request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /** Partially updates a CCP. */
    @Operation(summary = "Update CCP")
    @PatchMapping("/haccp/critical-control-points/{ccpId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateCcp(
            @PathVariable Long ccpId,
            @Valid @RequestBody UpdateCcpRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                ccpService.updateCcp(ccpId, request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /** Deletes a CCP by ID. */
    @Operation(summary = "Delete CCP")
    @DeleteMapping("/haccp/critical-control-points/{ccpId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> deleteCcp(@PathVariable Long ccpId, Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            ccpService.deleteCcp(ccpId, principal.userId(), principal.orgId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /** Replaces all user assignments for a CCP. */
    @Operation(summary = "Replace CCP assignments")
    @PutMapping("/haccp/critical-control-points/{ccpId}/assignments")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> replaceAssignments(
            @PathVariable Long ccpId,
            @RequestBody ReplaceCcpAssignmentsRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                ccpService.replaceAssignments(ccpId, request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /** Creates a corrective measure for a CCP. */
    @Operation(summary = "Create corrective measure")
    @PostMapping("/haccp/critical-control-points/{ccpId}/corrective-measures")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createCorrectiveMeasure(
            @PathVariable Long ccpId,
            @Valid @RequestBody CreateCcpCorrectiveMeasureRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ccpService.createCorrectiveMeasure(ccpId, request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /** Partially updates a corrective measure. */
    @Operation(summary = "Update corrective measure")
    @PatchMapping("/haccp/critical-control-points/corrective-measures/{measureId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateCorrectiveMeasure(
            @PathVariable Long measureId,
            @Valid @RequestBody UpdateCcpCorrectiveMeasureRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                ccpService.updateCorrectiveMeasure(measureId, request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /** Deletes a corrective measure by ID. */
    @Operation(summary = "Delete corrective measure")
    @DeleteMapping("/haccp/critical-control-points/corrective-measures/{measureId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> deleteCorrectiveMeasure(@PathVariable Long measureId, Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            ccpService.deleteCorrectiveMeasure(measureId, principal.userId(), principal.orgId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }
}
