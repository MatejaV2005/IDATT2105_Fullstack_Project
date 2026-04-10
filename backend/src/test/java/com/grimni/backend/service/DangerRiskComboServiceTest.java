package com.grimni.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.DangerRiskCombo;
import com.grimni.domain.Organization;
import com.grimni.domain.ProductCategory;
import com.grimni.dto.UpdateDangerRiskComboRequest;
import com.grimni.repository.DangerRiskComboRepository;
import com.grimni.service.DangerRiskComboService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class DangerRiskComboServiceTest {

    @Mock
    private DangerRiskComboRepository dangerRiskComboRepository;

    @InjectMocks
    private DangerRiskComboService dangerRiskComboService;

    private static final Long ORG_ID = 10L;
    private static final Long PRODUCT_CATEGORY_ID = 5L;
    private static final Long DANGER_RISK_COMBO_ID = 7L;

    private DangerRiskCombo existingCombo;

    @BeforeEach
    void setUp() {
        Organization org = new Organization();
        ReflectionTestUtils.setField(org, "id", ORG_ID);

        ProductCategory category = new ProductCategory();
        ReflectionTestUtils.setField(category, "id", PRODUCT_CATEGORY_ID);
        category.setProductName("Fisk og sjomat");
        category.setProductDescription("Fisk og sjomat");
        category.setOrganization(org);

        existingCombo = new DangerRiskCombo();
        ReflectionTestUtils.setField(existingCombo, "id", DANGER_RISK_COMBO_ID);
        existingCombo.setDanger("Old danger");
        existingCombo.setDangerCorrectiveMeasure("Old measure");
        existingCombo.setSeverityScore(1);
        existingCombo.setLikelihoodScore(1);
        existingCombo.setProductCategory(category);
    }

    @Test
    @DisplayName("updates all fields on the existing combo and saves it")
    void updateDangerRiskCombo_success() {
        UpdateDangerRiskComboRequest request = new UpdateDangerRiskComboRequest(
                PRODUCT_CATEGORY_ID,
                DANGER_RISK_COMBO_ID,
                "New danger",
                "New corrective measure",
                4,
                3
        );

        when(dangerRiskComboRepository.findByIdAndProductCategory_Organization_Id(DANGER_RISK_COMBO_ID, ORG_ID))
                .thenReturn(Optional.of(existingCombo));

        dangerRiskComboService.updateDangerRiskCombo(request, ORG_ID);

        ArgumentCaptor<DangerRiskCombo> captor = ArgumentCaptor.forClass(DangerRiskCombo.class);
        verify(dangerRiskComboRepository).save(captor.capture());

        DangerRiskCombo saved = captor.getValue();
        assertEquals("New danger", saved.getDanger());
        assertEquals("New corrective measure", saved.getDangerCorrectiveMeasure());
        assertEquals(4, saved.getSeverityScore());
        assertEquals(3, saved.getLikelihoodScore());
    }

    @Test
    @DisplayName("throws EntityNotFoundException when combo does not exist within the organization")
    void updateDangerRiskCombo_notFound() {
        UpdateDangerRiskComboRequest request = new UpdateDangerRiskComboRequest(
                PRODUCT_CATEGORY_ID,
                DANGER_RISK_COMBO_ID,
                "New danger",
                "New corrective measure",
                4,
                3
        );

        when(dangerRiskComboRepository.findByIdAndProductCategory_Organization_Id(DANGER_RISK_COMBO_ID, ORG_ID))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> dangerRiskComboService.updateDangerRiskCombo(request, ORG_ID));

        verify(dangerRiskComboRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws AccessDeniedException when productCategoryId does not match the combo's category")
    void updateDangerRiskCombo_categoryMismatch() {
        UpdateDangerRiskComboRequest request = new UpdateDangerRiskComboRequest(
                999L,
                DANGER_RISK_COMBO_ID,
                "New danger",
                "New corrective measure",
                4,
                3
        );

        when(dangerRiskComboRepository.findByIdAndProductCategory_Organization_Id(DANGER_RISK_COMBO_ID, ORG_ID))
                .thenReturn(Optional.of(existingCombo));

        assertThrows(AccessDeniedException.class,
                () -> dangerRiskComboService.updateDangerRiskCombo(request, ORG_ID));

        verify(dangerRiskComboRepository, never()).save(any());
    }
}
