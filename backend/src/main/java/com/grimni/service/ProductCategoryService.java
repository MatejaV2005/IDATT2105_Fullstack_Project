package com.grimni.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.dto.ProductCategoryResponse;
import com.grimni.repository.ProductCategoryRepository;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> getAllByOrg(Long orgId) {
        return productCategoryRepository.findByOrganization_Id(orgId).stream()
                .map(pc -> new ProductCategoryResponse(pc.getId(), pc.getProductDescription()))
                .toList();
    }
}
