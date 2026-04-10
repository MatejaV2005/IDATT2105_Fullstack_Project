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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.dto.CreateMappingPointRequest;
import com.grimni.dto.UpdateMappingPointRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.MappingPointService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for managing Mapping Points (kartleggingspunkter) within the HACCP danger analysis framework.
 * <p>
 * Mapping points represent specific stages or locations in the production process where potential
 * food safety hazards are identified and analyzed. This controller provides the interface for
 * defining the structural foundation of an organization's hazard analysis and critical control 
 * points (HACCP) system.
 * </p>
 */
@Tag(name = "Mapping Points", description = "CRUD for HACCP danger-analysis mapping points")
@RestController
public class MappingPointController {

    private final MappingPointService mappingPointService;

    public MappingPointController(MappingPointService mappingPointService) {
        this.mappingPointService = mappingPointService;
    }

    /**
     * Retrieves all mapping points and their detailed configurations for the caller's organization.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the full collection of mapping point data.
     * @throws RuntimeException if the resource collection cannot be retrieved.
     */
    @Operation(summary = "Get all mapping points")
    @GetMapping("/mapping-points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllInfo(Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                mappingPointService.getAllInfo(principal.userId(), principal.orgId())
            );
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Creates a new mapping point entry for the authenticated user's organization.
     * <p>
     * Restricted to administrative roles (OWNER, MANAGER) as these points define the 
     * fundamental HACCP workflow.
     * </p>
     *
     * @param request        The validated request DTO containing mapping point specifications.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the newly created mapping point and HTTP 201 status.
     * @throws IllegalArgumentException if the request data fails business logic validation.
     * @throws RuntimeException         if the creation process encounters a persistence or scoping error.
     */
    @Operation(summary = "Create mapping point")
    @PostMapping("/mapping-points")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createMappingPoint(
            @Valid @RequestBody CreateMappingPointRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.status(HttpStatus.CREATED).body(
                mappingPointService.createMappingPoint(request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Performs a partial update on an existing mapping point.
     *
     * @param mappingPointId The unique identifier of the mapping point to be updated.
     * @param request        The validated request DTO containing updated field values.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated mapping point resource.
     * @throws IllegalArgumentException if the update parameters are invalid.
     * @throws RuntimeException         if the target mapping point is not found within the user's organization.
     */
    @Operation(summary = "Update mapping point")
    @PatchMapping("/mapping-points/{mappingPointId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateMappingPoint(
            @PathVariable Long mappingPointId,
            @Valid @RequestBody UpdateMappingPointRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                mappingPointService.updateMappingPoint(mappingPointId, request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Permanently removes a mapping point from the organization's HACCP framework.
     *
     * @param mappingPointId The unique identifier of the mapping point to delete.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with HTTP 204 No Content status on successful deletion.
     * @throws RuntimeException if the mapping point cannot be found or the user lacks permission.
     */
    @Operation(summary = "Delete mapping point")
    @DeleteMapping("/mapping-points/{mappingPointId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> deleteMappingPoint(@PathVariable Long mappingPointId, Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            mappingPointService.deleteMappingPoint(mappingPointId, principal.userId(), principal.orgId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }
}