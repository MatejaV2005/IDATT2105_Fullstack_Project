package com.grimni.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.grimni.domain.FileObject;
import com.grimni.domain.Organization;
import com.grimni.domain.ProductCategory;
import com.grimni.domain.User;
import com.grimni.domain.enums.AccessLevel;
import com.grimni.dto.CreateProductCategoryRequest;
import com.grimni.dto.DangerAnalysisProductCategoryResponse;
import com.grimni.dto.ProductCategoryResponse;
import com.grimni.dto.UpdateProductCategoryRequest;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.ProductCategoryRepository;
import com.grimni.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

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
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final SimpleStorageService simpleStorageService;

    public ProductCategoryService(
            ProductCategoryRepository productCategoryRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            SimpleStorageService simpleStorageService) {
        this.productCategoryRepository = productCategoryRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.simpleStorageService = simpleStorageService;
    }

    /**
     * Retrieves all product categories associated with a specific organization.
     *
     * @param orgId the unique identifier of the organization whose categories are to be fetched.
     * @return a list of {@link ProductCategoryResponse} objects representing the classification schema.
     */
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> getAllByOrg(Long orgId) {
        return productCategoryRepository.findByOrganization_Id(orgId).stream()
                .map(pc -> new ProductCategoryResponse(pc.getId(), pc.getProductName(), pc.getProductDescription()))
                .toList();
    }

    /**
     * Retrieves all product categories for the given organization together with their
     * danger/risk combos, shaped for the danger-analysis desktop view.
     *
     * @param orgId the unique identifier of the organization.
     * @return a list of {@link DangerAnalysisProductCategoryResponse} entries, each containing
     *         the category metadata and its danger/risk combos.
     */
    @Transactional(readOnly = true)
    public List<DangerAnalysisProductCategoryResponse> getDangerAnalysisByOrg(Long orgId) {
        return productCategoryRepository.findByOrgIdFetchDangerRiskCombos(orgId).stream()
                .map(DangerAnalysisProductCategoryResponse::fromEntity)
                .toList();
    }

    /**
     * Creates a new product category for the caller's organization.
     *
     * @param request the validated creation request.
     * @param orgId   the unique identifier of the organization that will own the new category.
     * @return a {@link ProductCategoryResponse} describing the created category.
     * @throws EntityNotFoundException if the organization cannot be found.
     */
    @Transactional
    public ProductCategoryResponse createProductCategory(CreateProductCategoryRequest request, Long orgId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        ProductCategory category = new ProductCategory();
        category.setProductName(request.productName());
        category.setProductDescription(request.productDescription());
        category.setOrganization(organization);

        ProductCategory saved = productCategoryRepository.save(category);
        return new ProductCategoryResponse(saved.getId(), saved.getProductName(), saved.getProductDescription());
    }

    /**
     * Updates the name and description of an existing product category belonging to the
     * caller's organization.
     *
     * @param request the validated update request.
     * @param orgId   the unique identifier of the caller's organization.
     * @return a {@link ProductCategoryResponse} describing the updated category.
     * @throws EntityNotFoundException if no category with the given id exists within the organization.
     */
    @Transactional
    public ProductCategoryResponse updateProductCategory(UpdateProductCategoryRequest request, Long orgId) {
        ProductCategory category = productCategoryRepository
                .findByIdAndOrganization_Id(request.categoryId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product category not found"));

        category.setProductName(request.productName());
        category.setProductDescription(request.productDescription());

        ProductCategory saved = productCategoryRepository.save(category);
        return new ProductCategoryResponse(saved.getId(), saved.getProductName(), saved.getProductDescription());
    }

    /**
     * Uploads a flowchart file and attaches it to the product category belonging to the
     * caller's organization. Any previously attached flowchart is replaced.
     *
     * @param categoryId the id of the product category the file should be attached to.
     * @param file       the multipart file to upload.
     * @param orgId      the id of the caller's organization.
     * @param userId     the id of the uploading user.
     * @return the id of the newly created {@link FileObject}.
     */
    @Transactional
    public Long uploadFlowchart(Long categoryId, MultipartFile file, Long orgId, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        ProductCategory category = productCategoryRepository
                .findByIdAndOrganization_Id(categoryId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product category not found"));

        User uploadedBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Organization organization = category.getOrganization();

        String originalFilename = file.getOriginalFilename() == null
                ? "flowchart" : StringUtils.cleanPath(file.getOriginalFilename());
        String key = "product-categories/" + categoryId + "/flowchart/"
                + UUID.randomUUID() + "-" + originalFilename;

        try {
            FileObject savedFile = simpleStorageService.upload(
                    key,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType(),
                    AccessLevel.ANYONE_IN_ORG,
                    AccessLevel.MANAGER,
                    uploadedBy,
                    originalFilename,
                    organization
            );

            category.setFlowchartFile(savedFile);
            productCategoryRepository.save(category);

            return savedFile.getId();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", exception);
        }
    }
}
