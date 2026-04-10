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
import com.grimni.dto.UpdateCcpFullRequest;
import com.grimni.dto.UpdateCcpRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.CcpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for managing Critical Control Points (CCPs) within the HACCP compliance framework.
 * * <p>This controller provides endpoints for the full lifecycle of CCPs, including:
 * <ul>
 * <li>CRUD operations for CCP definitions</li>
 * <li>User assignment management</li>
 * <li>Corrective measure configuration</li>
 * <li>Verification monitoring and logging</li>
 * </ul>
 * * All operations are scoped by the user's organization and authenticated principal.
 */
@Tag(name = "Critical Control Points", description = "HACCP CCP management, assignments, and corrective measures")
@RestController
public class CcpController {

    private final CcpService ccpService;

    public CcpController(CcpService ccpService) {
        this.ccpService = ccpService;
    }

    /**
     * Retrieves the total count of CCP records currently awaiting verification.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the total count of pending verifications as a {@link Long}.
     */
    @Operation(summary = "Get verification count")
    @GetMapping("/ccps/verification-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getVerificationCount(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        long count = ccpService.getVerificationCount(principal.userId(), principal.orgId(), principal.role());
        return ResponseEntity.ok(count);
    }

    /**
     * Retrieves historical verification logs associated with CCPs.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing a list of verification logs filtered by user permissions.
     */
    @Operation(summary = "Get verification logs")
    @GetMapping("/ccps/logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getVerificationLogs(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(
            ccpService.getVerificationLogs(principal.userId(), principal.orgId(), principal.role())
        );
    }

    /**
     * Retrieves all CCP definitions and associated metadata for the caller's organization.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the full CCP data set.
     * @throws RuntimeException if the records cannot be located or accessed.
     */
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

    /**
     * Registers a new Critical Control Point.
     * * <p>Access restricted to OWNER and MANAGER roles.</p>
     *
     * @param request The data transfer object containing new CCP details.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the created CCP resource.
     * @throws IllegalArgumentException if the request data fails validation logic.
     * @throws RuntimeException if an internal error occurs during persistence.
     */
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

    /**
     * Performs a full update of a CCP including fields, user assignments, and corrective measures
     * in a single transactional operation.
     *
     * @param request The data transfer object containing the CCP id, all updatable fields,
     *                user assignment lists, and corrective measure entries.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the fully updated CCP resource.
     */
    @Operation(summary = "Update CCP (full)")
    @PutMapping("/haccp/critical-control-points")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateCcpFull(
            @Valid @RequestBody UpdateCcpFullRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                ccpService.updateCcpFull(request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Updates specific fields of an existing CCP.
     *
     * @param ccpId The unique identifier of the CCP to update.
     * @param request The data transfer object containing updated fields.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated CCP resource.
     */
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

    /**
     * Removes a CCP from the system.
     *
     * @param ccpId The unique identifier of the CCP to delete.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return A 204 No Content response on success.
     */
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

    /**
     * Overwrites the current set of user assignments for a specific CCP.
     *
     * @param ccpId The unique identifier of the target CCP.
     * @param request The object containing the new set of assigned user IDs.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} reflecting the new state of assignments.
     */
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

    /**
     * Appends a new corrective measure configuration to a specific CCP.
     *
     * @param ccpId The unique identifier of the CCP.
     * @param request The data transfer object for the corrective measure.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the created corrective measure.
     */
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

    /**
     * Updates an existing corrective measure's parameters.
     *
     * @param measureId The unique identifier of the corrective measure.
     * @param request The data transfer object containing update values.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated corrective measure.
     */
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

    /**
     * Deletes a specific corrective measure by its unique identifier.
     *
     * @param measureId The unique identifier of the corrective measure to remove.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return A 204 No Content response on success.
     */
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