package com.grimni.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.grimni.dto.CreateProductCategoryRequest;
import com.grimni.dto.ProductCategoryResponse;
import com.grimni.dto.UpdateProductCategoryRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.ProductCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
    @GetMapping("/product-categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(productCategoryService.getAllByOrg(principal.orgId()));
    }

    /**
     * Creates a new product category within the caller's organization.
     *
     * @param request        The validated request containing the product name and description.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the created {@link ProductCategoryResponse}.
     */
    @Operation(summary = "Create product category")
    @PostMapping("/product-categories")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createProductCategory(
            @Valid @RequestBody CreateProductCategoryRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        ProductCategoryResponse response =
                productCategoryService.createProductCategory(request, principal.orgId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all product categories for the caller's organization together with their
     * danger/risk combos, shaped for the danger-analysis desktop view.
     *
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the danger-analysis product category list.
     */
    /**
     * Updates the name and description of an existing product category within the caller's organization.
     *
     * @param request        The validated request containing the category id, product name and description.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated {@link ProductCategoryResponse}.
     */
    @Operation(summary = "Update product category")
    @PatchMapping("/product-categories")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateProductCategory(
            @Valid @RequestBody UpdateProductCategoryRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        ProductCategoryResponse response =
                productCategoryService.updateProductCategory(request, principal.orgId());
        return ResponseEntity.ok(response);
    }

    /**
     * Uploads a flowchart file and attaches it to a product category belonging to the
     * caller's organization.
     *
     * @param categoryId     The id of the product category the file should be attached to.
     * @param file           The multipart file to upload.
     * @param authentication The security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the id of the saved file.
     */
    @Operation(summary = "Upload product category flowchart")
    @PostMapping("/product-categories/{categoryId}/flowchart")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> uploadFlowchart(
            @PathVariable Long categoryId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Long fileId = productCategoryService.uploadFlowchart(
                categoryId, file, principal.orgId(), principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(fileId);
    }

    @Operation(summary = "List product categories with danger-analysis data")
    @GetMapping("/product-categories/danger-analysis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDangerAnalysis(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(productCategoryService.getDangerAnalysisByOrg(principal.orgId()));
    }
}