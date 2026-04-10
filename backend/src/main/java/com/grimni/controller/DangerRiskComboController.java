package com.grimni.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.dto.CreateDangerRiskComboRequest;
import com.grimni.dto.DangerRiskComboResponse;
import com.grimni.dto.DeleteDangerRiskComboRequest;
import com.grimni.dto.UpdateDangerRiskComboRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.DangerRiskComboService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Danger Risk Combos", description = "HACCP danger/risk combinations per product category")
@RestController
@RequestMapping("/danger-risk-combos")
public class DangerRiskComboController {

    private final DangerRiskComboService dangerRiskComboService;

    public DangerRiskComboController(DangerRiskComboService dangerRiskComboService) {
        this.dangerRiskComboService = dangerRiskComboService;
    }

    @Operation(summary = "Create danger/risk combo")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createDangerRiskCombo(
            @Valid @RequestBody CreateDangerRiskComboRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        DangerRiskComboResponse response =
                dangerRiskComboService.createDangerRiskCombo(request, principal.orgId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update danger/risk combo")
    @PatchMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateDangerRiskCombo(
            @Valid @RequestBody UpdateDangerRiskComboRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        dangerRiskComboService.updateDangerRiskCombo(request, principal.orgId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete danger/risk combo")
    @DeleteMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> deleteDangerRiskCombo(
            @Valid @RequestBody DeleteDangerRiskComboRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        dangerRiskComboService.deleteDangerRiskCombo(request, principal.orgId());
        return ResponseEntity.noContent().build();
    }
}
