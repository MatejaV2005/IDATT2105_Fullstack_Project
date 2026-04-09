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

import jakarta.validation.Valid;

@RestController
public class MappingPointController {

    private final MappingPointService mappingPointService;

    public MappingPointController(MappingPointService mappingPointService) {
        this.mappingPointService = mappingPointService;
    }

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
