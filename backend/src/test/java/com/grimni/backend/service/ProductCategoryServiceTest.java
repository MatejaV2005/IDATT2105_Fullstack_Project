package com.grimni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.DangerRiskCombo;
import com.grimni.domain.Organization;
import com.grimni.domain.ProductCategory;
import com.grimni.dto.CreateProductCategoryRequest;
import com.grimni.dto.DangerAnalysisProductCategoryResponse;
import com.grimni.dto.DangerRiskComboResponse;
import com.grimni.dto.ProductCategoryResponse;
import com.grimni.dto.UpdateProductCategoryRequest;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.ProductCategoryRepository;
import com.grimni.service.ProductCategoryService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ProductCategoryServiceTest {

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private ProductCategoryService productCategoryService;

    private ProductCategory buildCategory(Long id, String description) {
        Organization org = new Organization();
        ReflectionTestUtils.setField(org, "id", 10L);

        ProductCategory pc = new ProductCategory();
        ReflectionTestUtils.setField(pc, "id", id);
        pc.setProductName(description);
        pc.setProductDescription(description);
        pc.setOrganization(org);
        return pc;
    }

    @Test
    @DisplayName("returns all product categories for the organization")
    void getAllByOrg_success() {
        when(productCategoryRepository.findByOrganization_Id(10L)).thenReturn(List.of(
                buildCategory(1L, "Fisk og sjomat"),
                buildCategory(2L, "Kjott og farseprodukter")
        ));

        List<ProductCategoryResponse> result = productCategoryService.getAllByOrg(10L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Fisk og sjomat", result.get(0).productName());
        assertEquals("Fisk og sjomat", result.get(0).productDescription());
        assertEquals(2L, result.get(1).id());
        assertEquals("Kjott og farseprodukter", result.get(1).productName());
        assertEquals("Kjott og farseprodukter", result.get(1).productDescription());
    }

    @Test
    @DisplayName("returns empty list when organization has no product categories")
    void getAllByOrg_empty() {
        when(productCategoryRepository.findByOrganization_Id(10L)).thenReturn(List.of());

        List<ProductCategoryResponse> result = productCategoryService.getAllByOrg(10L);

        assertTrue(result.isEmpty());
    }

    private DangerRiskCombo buildCombo(
            Long id,
            String danger,
            String measure,
            int severity,
            int likelihood,
            LocalDateTime createdAt
    ) {
        DangerRiskCombo combo = new DangerRiskCombo();
        ReflectionTestUtils.setField(combo, "id", id);
        combo.setDanger(danger);
        combo.setDangerCorrectiveMeasure(measure);
        combo.setSeverityScore(severity);
        combo.setLikelihoodScore(likelihood);
        ReflectionTestUtils.setField(combo, "createdAt", createdAt);
        return combo;
    }

    @Test
    @DisplayName("returns danger-analysis categories with nested combos and null file metadata")
    void getDangerAnalysisByOrg_success() {
        LocalDateTime categoryCreatedAt = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime comboCreatedAt = LocalDateTime.of(2025, 1, 2, 12, 0);

        ProductCategory category = buildCategory(1L, "Kyllingsalat");
        ReflectionTestUtils.setField(category, "createdAt", categoryCreatedAt);
        category.setDangerRiskCombos(List.of(
                buildCombo(100L, "Salmonella", "Varmebehandle", 3, 2, comboCreatedAt),
                buildCombo(101L, "Listeria", "Kjol ned", 2, 2, comboCreatedAt)
        ));

        when(productCategoryRepository.findByOrgIdFetchDangerRiskCombos(10L))
                .thenReturn(List.of(category));

        List<DangerAnalysisProductCategoryResponse> result =
                productCategoryService.getDangerAnalysisByOrg(10L);

        assertEquals(1, result.size());
        DangerAnalysisProductCategoryResponse response = result.get(0);
        assertEquals(1L, response.id());
        assertEquals("Kyllingsalat", response.productName());
        assertEquals("Kyllingsalat", response.productDescription());
        assertNull(response.flowchartFileId());
        assertNull(response.flowchartFileName());
        assertNull(response.flowchartPreviewUrl());
        assertEquals(categoryCreatedAt, response.createdAt());

        List<DangerRiskComboResponse> combos = response.dangerRiskCombos();
        assertEquals(2, combos.size());
        assertEquals(100L, combos.get(0).id());
        assertEquals("Salmonella", combos.get(0).danger());
        assertEquals("Varmebehandle", combos.get(0).dangerCorrectiveMeasure());
        assertEquals(3, combos.get(0).severityScore());
        assertEquals(2, combos.get(0).likelihoodScore());
        assertEquals(comboCreatedAt, combos.get(0).createdAt());
        assertEquals(101L, combos.get(1).id());
        assertEquals("Listeria", combos.get(1).danger());
    }

    @Test
    @DisplayName("returns empty list when organization has no product categories for danger analysis")
    void getDangerAnalysisByOrg_empty() {
        when(productCategoryRepository.findByOrgIdFetchDangerRiskCombos(10L)).thenReturn(List.of());

        List<DangerAnalysisProductCategoryResponse> result =
                productCategoryService.getDangerAnalysisByOrg(10L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("returns category with empty combos list when none exist")
    void getDangerAnalysisByOrg_categoryWithNoCombos() {
        ProductCategory category = buildCategory(2L, "Husets burger");
        category.setDangerRiskCombos(List.of());

        when(productCategoryRepository.findByOrgIdFetchDangerRiskCombos(10L))
                .thenReturn(List.of(category));

        List<DangerAnalysisProductCategoryResponse> result =
                productCategoryService.getDangerAnalysisByOrg(10L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).dangerRiskCombos().isEmpty());
    }

    @Test
    @DisplayName("creates a product category and returns it mapped to a response")
    void createProductCategory_success() {
        Organization org = new Organization();
        ReflectionTestUtils.setField(org, "id", 10L);

        CreateProductCategoryRequest request = new CreateProductCategoryRequest(
                "Kyllingsalat",
                "Fersk kylling med majones"
        );

        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(productCategoryRepository.save(any(ProductCategory.class))).thenAnswer(invocation -> {
            ProductCategory pc = invocation.getArgument(0);
            ReflectionTestUtils.setField(pc, "id", 42L);
            return pc;
        });

        ProductCategoryResponse result = productCategoryService.createProductCategory(request, 10L);

        ArgumentCaptor<ProductCategory> captor = ArgumentCaptor.forClass(ProductCategory.class);
        verify(productCategoryRepository).save(captor.capture());

        ProductCategory saved = captor.getValue();
        assertEquals("Kyllingsalat", saved.getProductName());
        assertEquals("Fersk kylling med majones", saved.getProductDescription());
        assertSame(org, saved.getOrganization());

        assertEquals(42L, result.id());
        assertEquals("Kyllingsalat", result.productName());
        assertEquals("Fersk kylling med majones", result.productDescription());
    }

    @Test
    @DisplayName("throws EntityNotFoundException when the organization does not exist")
    void createProductCategory_organizationNotFound() {
        CreateProductCategoryRequest request = new CreateProductCategoryRequest(
                "Kyllingsalat",
                "Fersk kylling med majones"
        );

        when(organizationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productCategoryService.createProductCategory(request, 10L));

        verify(productCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("updates an existing product category and returns the updated response")
    void updateProductCategory_success() {
        ProductCategory existing = buildCategory(42L, "Gammelt navn");

        UpdateProductCategoryRequest request = new UpdateProductCategoryRequest(
                42L,
                "Nytt navn",
                "Ny beskrivelse"
        );

        when(productCategoryRepository.findByIdAndOrganization_Id(42L, 10L))
                .thenReturn(Optional.of(existing));
        when(productCategoryRepository.save(any(ProductCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductCategoryResponse result = productCategoryService.updateProductCategory(request, 10L);

        ArgumentCaptor<ProductCategory> captor = ArgumentCaptor.forClass(ProductCategory.class);
        verify(productCategoryRepository).save(captor.capture());

        ProductCategory saved = captor.getValue();
        assertEquals("Nytt navn", saved.getProductName());
        assertEquals("Ny beskrivelse", saved.getProductDescription());

        assertEquals(42L, result.id());
        assertEquals("Nytt navn", result.productName());
        assertEquals("Ny beskrivelse", result.productDescription());
    }

    @Test
    @DisplayName("throws EntityNotFoundException when the product category does not exist in the organization")
    void updateProductCategory_notFound() {
        UpdateProductCategoryRequest request = new UpdateProductCategoryRequest(
                42L,
                "Nytt navn",
                "Ny beskrivelse"
        );

        when(productCategoryRepository.findByIdAndOrganization_Id(42L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productCategoryService.updateProductCategory(request, 10L));

        verify(productCategoryRepository, never()).save(any());
    }
}
