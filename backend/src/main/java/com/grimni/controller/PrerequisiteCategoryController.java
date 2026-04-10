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

import com.grimni.dto.CreatePrerequisiteCategoryRequest;
import com.grimni.dto.CreatePrerequisiteRoutineRequest;
import com.grimni.dto.CreatePrerequisiteStandardRequest;
import com.grimni.dto.ReplaceRoutineAssignmentsRequest;
import com.grimni.dto.UpdatePrerequisiteCategoryRequest;
import com.grimni.dto.UpdatePrerequisiteRoutineRequest;
import com.grimni.dto.UpdatePrerequisiteStandardRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.PrerequisiteCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for managing the hierarchical Prerequisite Program (PRP) structure within a HACCP framework.
 * <p>
 * This controller orchestrates the management of:
 * <ul>
 * <li><b>Categories:</b> High-level groupings for compliance areas (e.g., Cleaning, Pest Control).</li>
 * <li><b>Standards:</b> Static regulatory or internal requirements that must be met.</li>
 * <li><b>Routines:</b> Recurring operational tasks with defined intervals and user assignments.</li>
 * </ul>
 * It ensures multi-tenant data integrity by scoping all operations to the authenticated user's organization.
 */
@Tag(name = "Prerequisite Categories", description = "CRUD for prerequisite categories, standards, routines, and assignments")
@RestController
public class PrerequisiteCategoryController {

    private final PrerequisiteCategoryService prerequisiteCategoryService;

    public PrerequisiteCategoryController(PrerequisiteCategoryService prerequisiteCategoryService) {
        this.prerequisiteCategoryService = prerequisiteCategoryService;
    }

    /**
     * Retrieves the complete hierarchy of prerequisite categories, including nested standards and routines.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the full prerequisite data structure for the organization.
     */
    @Operation(summary = "Get all prerequisite info")
    @GetMapping("/prerequisite-categories/get-all-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllInfo(Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                prerequisiteCategoryService.getAllInfo(principal.userId(), principal.orgId())
            );
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Creates a new high-level prerequisite category.
     *
     * @param request        Validated DTO containing the category details.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the created category with HTTP 201 status.
     */
    @Operation(summary = "Create category")
    @PostMapping("/prerequisite-categories/create-category")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody CreatePrerequisiteCategoryRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.status(HttpStatus.CREATED).body(
                prerequisiteCategoryService.createCategory(request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Updates an existing prerequisite category's attributes.
     *
     * @param request        Validated update request containing the target category ID.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated category resource.
     */
    @Operation(summary = "Update category")
    @PatchMapping("/prerequisite-categories/update-category")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateCategory(
            @Valid @RequestBody UpdatePrerequisiteCategoryRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                prerequisiteCategoryService.updateCategory(request.categoryId(), request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Permanently deletes a prerequisite category and its associated children.
     *
     * @param categoryId     The unique identifier of the category to remove.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with HTTP 204 No Content status on success.
     */
    @Operation(summary = "Delete category")
    @DeleteMapping("/prerequisite-categories/{categoryId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId, Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            prerequisiteCategoryService.deleteCategory(categoryId, principal.userId(), principal.orgId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Adds a static prerequisite standard to a specific category.
     *
     * @param request        The validated standard creation data, including category reference.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the created standard.
     */
    @Operation(summary = "Create standard")
    @PostMapping("/prerequisite-categories/create-standard")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createStandard(
            @Valid @RequestBody CreatePrerequisiteStandardRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.status(HttpStatus.CREATED).body(
                prerequisiteCategoryService.createStandard(request.categoryId(), request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Updates an existing prerequisite standard definition.
     *
     * @param request        Validated update request containing the standard ID.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated standard resource.
     */
    @Operation(summary = "Update standard")
    @PatchMapping("/prerequisite-categories/update-standard")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateStandard(
            @Valid @RequestBody UpdatePrerequisiteStandardRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                prerequisiteCategoryService.updateStandard(request.standardId(), request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Removes a prerequisite standard from the system.
     *
     * @param standardId     The unique identifier of the standard.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with HTTP 204 No Content status.
     */
    @Operation(summary = "Delete standard")
    @DeleteMapping("/prerequisite-standards/{standardId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> deleteStandard(@PathVariable Long standardId, Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            prerequisiteCategoryService.deleteStandard(standardId, principal.userId(), principal.orgId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Registers a new recurring routine with execution intervals and personnel assignments.
     *
     * @param request        Validated request containing routine logic and category binding.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the created routine resource.
     */
    @Operation(summary = "Create routine")
    @PostMapping("/prerequisite-categories/create-routine")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createRoutine(
            @Valid @RequestBody CreatePrerequisiteRoutineRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.status(HttpStatus.CREATED).body(
                prerequisiteCategoryService.createRoutine(request.categoryId(), request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Updates an existing routine's operational parameters or user assignments.
     *
     * @param request        Validated update request containing the routine ID.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated routine.
     */
    @Operation(summary = "Update routine")
    @PatchMapping("/prerequisite-categories/update-routine")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateRoutine(
            @Valid @RequestBody UpdatePrerequisiteRoutineRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                prerequisiteCategoryService.updateRoutine(request.routineId(), request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Permanently deletes a recurring prerequisite routine.
     *
     * @param routineId      The unique identifier of the routine.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with HTTP 204 No Content status.
     */
    @Operation(summary = "Delete routine")
    @DeleteMapping("/prerequisite-routines/{routineId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> deleteRoutine(@PathVariable Long routineId, Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            prerequisiteCategoryService.deleteRoutine(routineId, principal.userId(), principal.orgId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    /**
     * Overwrites all existing user assignments (Performers, Deputies, Verifiers) for a specific routine.
     *
     * @param routineId      The unique identifier of the target routine.
     * @param request        The request containing the new collection of user assignments.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} reflecting the updated state of routine assignments.
     */
    @Operation(summary = "Replace routine assignments")
    @PutMapping("/prerequisite-routines/{routineId}/assignments")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> replaceRoutineAssignments(
            @PathVariable Long routineId,
            @RequestBody ReplaceRoutineAssignmentsRequest request,
            Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            return ResponseEntity.ok(
                prerequisiteCategoryService.replaceRoutineAssignments(routineId, request, principal.userId(), principal.orgId())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }
}