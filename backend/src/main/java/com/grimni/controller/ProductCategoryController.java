package com.grimni.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.ProductCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Provides read access to product categories within an organization.
 */
@Tag(name = "Product Categories", description = "List product categories for the organization")
@RestController
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    /** Returns all product categories for the caller's organization. */
    @Operation(summary = "List product categories")
    @GetMapping("/api/product-categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(productCategoryService.getAllByOrg(principal.orgId()));
    }
}
