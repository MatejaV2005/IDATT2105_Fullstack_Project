package com.grimni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.Organization;
import com.grimni.domain.ProductCategory;
import com.grimni.dto.ProductCategoryResponse;
import com.grimni.repository.ProductCategoryRepository;
import com.grimni.service.ProductCategoryService;

@ExtendWith(MockitoExtension.class)
public class ProductCategoryServiceTest {

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @InjectMocks
    private ProductCategoryService productCategoryService;

    private ProductCategory buildCategory(Long id, String description) {
        Organization org = new Organization();
        ReflectionTestUtils.setField(org, "id", 10L);

        ProductCategory pc = new ProductCategory();
        ReflectionTestUtils.setField(pc, "id", id);
        pc.setProductDescription(description);
        pc.setOrganization(org);
        pc.setFlowchart("{}");
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
        assertEquals("Fisk og sjomat", result.get(0).productDescription());
        assertEquals(2L, result.get(1).id());
        assertEquals("Kjott og farseprodukter", result.get(1).productDescription());
    }

    @Test
    @DisplayName("returns empty list when organization has no product categories")
    void getAllByOrg_empty() {
        when(productCategoryRepository.findByOrganization_Id(10L)).thenReturn(List.of());

        List<ProductCategoryResponse> result = productCategoryService.getAllByOrg(10L);

        assertTrue(result.isEmpty());
    }
}
