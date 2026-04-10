package com.grimni.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.dto.ProductCategoryResponse;
import com.grimni.repository.ProductCategoryRepository;

/**
 * Service class for managing and retrieving product categories within an organization.
 * <p>
 * Product categories serve as a classification system for inventory and safety monitoring,
 * allowing the HACCP system to apply specific corrective measures or danger analyses
 * based on the type of product involved.
 * </p>
 */
@Service
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    /**
     * Retrieves all product categories associated with a specific organization.
     * <p>
     * This method maps the internal domain entities to a streamlined response DTO
     * containing only the necessary identification and descriptive metadata.
     * </p>
     *
     * @param orgId the unique identifier of the organization whose categories are to be fetched.
     * @return a list of {@link ProductCategoryResponse} objects representing the classification schema.
     */
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> getAllByOrg(Long orgId) {
        return productCategoryRepository.findByOrganization_Id(orgId).stream()
                .map(pc -> new ProductCategoryResponse(pc.getId(), pc.getProductDescription()))
                .toList();
    }
}