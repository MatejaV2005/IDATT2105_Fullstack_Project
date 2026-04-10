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
 * REST controller providing read-only access to product categories.
 * <p>
 * This controller allows users to retrieve the classification schema for products 
 * within their organization. Product categories are essential for inventory management, 
 * danger analysis, and organizational reporting within the HACCP system.
 * </p>
 */
@Tag(name = "Product Categories", description = "List product categories for the organization")
@RestController
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    /**
     * Retrieves a comprehensive list of all product categories associated with the caller's organization.
     * <p>
     * Access is granted to any authenticated user within the organization to ensure 
     * visibility across various operational workflows.
     * </p>
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing a collection of product category data.
     */
    @Operation(summary = "List product categories")
    @GetMapping("/api/product-categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(productCategoryService.getAllByOrg(principal.orgId()));
    }
}