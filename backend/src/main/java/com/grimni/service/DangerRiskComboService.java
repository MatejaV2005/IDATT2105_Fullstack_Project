package com.grimni.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.domain.DangerRiskCombo;
import com.grimni.domain.ProductCategory;
import com.grimni.dto.CreateDangerRiskComboRequest;
import com.grimni.dto.DangerRiskComboResponse;
import com.grimni.dto.DeleteDangerRiskComboRequest;
import com.grimni.dto.UpdateDangerRiskComboRequest;
import com.grimni.repository.DangerRiskComboRepository;
import com.grimni.repository.ProductCategoryRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DangerRiskComboService {

    private final DangerRiskComboRepository dangerRiskComboRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public DangerRiskComboService(
            DangerRiskComboRepository dangerRiskComboRepository,
            ProductCategoryRepository productCategoryRepository) {
        this.dangerRiskComboRepository = dangerRiskComboRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    @Transactional
    public DangerRiskComboResponse createDangerRiskCombo(CreateDangerRiskComboRequest request, Long orgId) {
        ProductCategory category = productCategoryRepository
            .findByIdAndOrganization_Id(request.productCategoryId(), orgId)
            .orElseThrow(() -> new EntityNotFoundException("Product category not found"));

        DangerRiskCombo combo = new DangerRiskCombo();
        combo.setProductCategory(category);
        combo.setDanger(request.danger());
        combo.setDangerCorrectiveMeasure(request.dangerCorrectiveMeasure());
        combo.setSeverityScore(request.severityScore());
        combo.setLikelihoodScore(request.likelihoodScore());

        DangerRiskCombo saved = dangerRiskComboRepository.save(combo);
        return DangerRiskComboResponse.fromEntity(saved);
    }

    @Transactional
    public void updateDangerRiskCombo(UpdateDangerRiskComboRequest request, Long orgId) {
        DangerRiskCombo combo = dangerRiskComboRepository
            .findByIdAndProductCategory_Organization_Id(request.dangerRiskComboId(), orgId)
            .orElseThrow(() -> new EntityNotFoundException("Danger/risk combo not found"));

        if (!combo.getProductCategory().getId().equals(request.productCategoryId())) {
            throw new AccessDeniedException("Danger/risk combo does not belong to the given product category");
        }

        combo.setDanger(request.danger());
        combo.setDangerCorrectiveMeasure(request.dangerCorrectiveMeasure());
        combo.setSeverityScore(request.severityScore());
        combo.setLikelihoodScore(request.likelihoodScore());

        dangerRiskComboRepository.save(combo);
    }

    @Transactional
    public void deleteDangerRiskCombo(DeleteDangerRiskComboRequest request, Long orgId) {
        DangerRiskCombo combo = dangerRiskComboRepository
            .findByIdAndProductCategory_Organization_Id(request.dangerRiskComboId(), orgId)
            .orElseThrow(() -> new EntityNotFoundException("Danger/risk combo not found"));

        dangerRiskComboRepository.delete(combo);
    }
}
