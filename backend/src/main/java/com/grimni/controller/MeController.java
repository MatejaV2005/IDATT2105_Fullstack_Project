package com.grimni.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.dto.OrganizationResponse;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.OrganizationService;

@RestController
@RequestMapping("/me")
public class MeController {

    private final OrganizationService organizationService;

    public MeController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/organizations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyOrganizations(Authentication authentication) {
        try {
            JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
            List<OrganizationResponse> orgs = organizationService.findOrganizationsByUserId(principal.userId())
                    .stream()
                    .map(OrganizationResponse::fromEntity)
                    .toList();
            return ResponseEntity.ok(orgs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}