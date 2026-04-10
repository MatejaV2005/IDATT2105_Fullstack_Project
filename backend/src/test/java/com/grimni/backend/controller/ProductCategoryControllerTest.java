package com.grimni.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.grimni.controller.ProductCategoryController;
import com.grimni.dto.CreateProductCategoryRequest;
import com.grimni.dto.DangerAnalysisProductCategoryResponse;
import com.grimni.dto.DangerRiskComboResponse;
import com.grimni.dto.ProductCategoryResponse;
import com.grimni.dto.UpdateProductCategoryRequest;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.MediaType;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.security.SecurityConfig;
import com.grimni.service.ProductCategoryService;
import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(ProductCategoryController.class)
@Import(SecurityConfig.class)
public class ProductCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductCategoryService productCategoryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
    }

    private static UsernamePasswordAuthenticationToken authWithRole(String role) {
        JwtUserPrinciple principal = new JwtUserPrinciple(1L, 10L, "alice", role);
        return new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    @DisplayName("returns product categories for authenticated user's organization")
    void getAll_success() throws Exception {
        when(productCategoryService.getAllByOrg(10L)).thenReturn(List.of(
                new ProductCategoryResponse(1L, "Fisk og sjomat", "Fisk og sjomat"),
                new ProductCategoryResponse(2L, "Kjott og farseprodukter", "Kjott og farseprodukter")
        ));

        mockMvc.perform(get("/product-categories")
                .with(authentication(authWithRole("WORKER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].productName").value("Fisk og sjomat"))
            .andExpect(jsonPath("$[0].productDescription").value("Fisk og sjomat"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].productName").value("Kjott og farseprodukter"))
            .andExpect(jsonPath("$[1].productDescription").value("Kjott og farseprodukter"));
    }

    @Test
    @DisplayName("returns empty list when no product categories exist")
    void getAll_empty() throws Exception {
        when(productCategoryService.getAllByOrg(10L)).thenReturn(List.of());

        mockMvc.perform(get("/product-categories")
                .with(authentication(authWithRole("WORKER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("unauthenticated request returns 403")
    void getAll_unauthenticated() throws Exception {
        mockMvc.perform(get("/product-categories"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("returns danger-analysis categories with nested combos for authenticated user")
    void getDangerAnalysis_success() throws Exception {
        LocalDateTime categoryCreatedAt = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime comboCreatedAt = LocalDateTime.of(2025, 1, 2, 12, 0);

        DangerRiskComboResponse combo = new DangerRiskComboResponse(
                100L, "Salmonella", "Varmebehandle", 3, 2, comboCreatedAt);

        DangerAnalysisProductCategoryResponse category = new DangerAnalysisProductCategoryResponse(
                1L,
                "Kyllingsalat",
                "Kyllingsalat",
                null,
                null,
                null,
                categoryCreatedAt,
                List.of(combo)
        );

        when(productCategoryService.getDangerAnalysisByOrg(10L)).thenReturn(List.of(category));

        mockMvc.perform(get("/product-categories/danger-analysis")
                .with(authentication(authWithRole("WORKER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].productName").value("Kyllingsalat"))
            .andExpect(jsonPath("$[0].productDescription").value("Kyllingsalat"))
            .andExpect(jsonPath("$[0].flowchartFileId").isEmpty())
            .andExpect(jsonPath("$[0].flowchartFileName").isEmpty())
            .andExpect(jsonPath("$[0].flowchartPreviewUrl").isEmpty())
            .andExpect(jsonPath("$[0].createdAt").exists())
            .andExpect(jsonPath("$[0].dangerRiskCombos[0].id").value(100))
            .andExpect(jsonPath("$[0].dangerRiskCombos[0].danger").value("Salmonella"))
            .andExpect(jsonPath("$[0].dangerRiskCombos[0].dangerCorrectiveMeasure").value("Varmebehandle"))
            .andExpect(jsonPath("$[0].dangerRiskCombos[0].severityScore").value(3))
            .andExpect(jsonPath("$[0].dangerRiskCombos[0].likelihoodScore").value(2));
    }

    @Test
    @DisplayName("returns empty list when no danger-analysis categories exist")
    void getDangerAnalysis_empty() throws Exception {
        when(productCategoryService.getDangerAnalysisByOrg(10L)).thenReturn(List.of());

        mockMvc.perform(get("/product-categories/danger-analysis")
                .with(authentication(authWithRole("WORKER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("unauthenticated danger-analysis request returns 403")
    void getDangerAnalysis_unauthenticated() throws Exception {
        mockMvc.perform(get("/product-categories/danger-analysis"))
            .andExpect(status().isForbidden());
    }

    private static final String VALID_CREATE_BODY = """
            {
                "productName": "fewfewew",
                "productDescription": "fekjfewnkfewn"
            }
            """;

    @Test
    @DisplayName("creates a product category and returns 201 when caller is OWNER")
    void createProductCategory_success_asOwner() throws Exception {
        when(productCategoryService.createProductCategory(
                any(CreateProductCategoryRequest.class), eq(10L)))
                .thenReturn(new ProductCategoryResponse(42L, "fewfewew", "fekjfewnkfewn"));

        mockMvc.perform(post("/product-categories")
                .with(authentication(authWithRole("OWNER")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.productName").value("fewfewew"))
            .andExpect(jsonPath("$.productDescription").value("fekjfewnkfewn"));
    }

    @Test
    @DisplayName("creates a product category and returns 201 when caller is MANAGER")
    void createProductCategory_success_asManager() throws Exception {
        when(productCategoryService.createProductCategory(
                any(CreateProductCategoryRequest.class), eq(10L)))
                .thenReturn(new ProductCategoryResponse(43L, "fewfewew", "fekjfewnkfewn"));

        mockMvc.perform(post("/product-categories")
                .with(authentication(authWithRole("MANAGER")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("returns 404 when the organization is not found")
    void createProductCategory_organizationNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Organization not found"))
                .when(productCategoryService)
                .createProductCategory(any(CreateProductCategoryRequest.class), eq(10L));

        mockMvc.perform(post("/product-categories")
                .with(authentication(authWithRole("OWNER")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 400 when required fields are missing")
    void createProductCategory_validationFailure() throws Exception {
        String invalidBody = """
                {
                    "productName": "",
                    "productDescription": ""
                }
                """;

        mockMvc.perform(post("/product-categories")
                .with(authentication(authWithRole("OWNER")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(productCategoryService);
    }

    @Test
    @DisplayName("returns 403 when caller is WORKER")
    void createProductCategory_forbidden_asWorker() throws Exception {
        mockMvc.perform(post("/product-categories")
                .with(authentication(authWithRole("WORKER")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
            .andExpect(status().isForbidden());

        verifyNoInteractions(productCategoryService);
    }

    @Test
    @DisplayName("returns 403 when unauthenticated")
    void createProductCategory_unauthenticated() throws Exception {
        mockMvc.perform(post("/product-categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
            .andExpect(status().isForbidden());

        verifyNoInteractions(productCategoryService);
    }

    private static final String VALID_UPDATE_BODY = """
            {
                "categoryId": 42,
                "productName": "Nytt navn",
                "productDescription": "Ny beskrivelse"
            }
            """;

    @Test
    @DisplayName("updates a product category and returns 200 when caller is OWNER")
    void updateProductCategory_success_asOwner() throws Exception {
        when(productCategoryService.updateProductCategory(
                any(UpdateProductCategoryRequest.class), eq(10L)))
                .thenReturn(new ProductCategoryResponse(42L, "Nytt navn", "Ny beskrivelse"));

        mockMvc.perform(patch("/product-categories")
                .with(authentication(authWithRole("OWNER")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.productName").value("Nytt navn"))
            .andExpect(jsonPath("$.productDescription").value("Ny beskrivelse"));
    }

    @Test
    @DisplayName("updates a product category and returns 200 when caller is MANAGER")
    void updateProductCategory_success_asManager() throws Exception {
        when(productCategoryService.updateProductCategory(
                any(UpdateProductCategoryRequest.class), eq(10L)))
                .thenReturn(new ProductCategoryResponse(42L, "Nytt navn", "Ny beskrivelse"));

        mockMvc.perform(patch("/product-categories")
                .with(authentication(authWithRole("MANAGER")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when the product category is not found")
    void updateProductCategory_notFound() throws Exception {
        doThrow(new EntityNotFoundException("Product category not found"))
                .when(productCategoryService)
                .updateProductCategory(any(UpdateProductCategoryRequest.class), eq(10L));

        mockMvc.perform(patch("/product-categories")
                .with(authentication(authWithRole("OWNER")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 400 when required update fields are missing")
    void updateProductCategory_validationFailure() throws Exception {
        String invalidBody = """
                {
                    "categoryId": null,
                    "productName": "",
                    "productDescription": ""
                }
                """;

        mockMvc.perform(patch("/product-categories")
                .with(authentication(authWithRole("OWNER")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(productCategoryService);
    }

    @Test
    @DisplayName("returns 403 when WORKER attempts to update a product category")
    void updateProductCategory_forbidden_asWorker() throws Exception {
        mockMvc.perform(patch("/product-categories")
                .with(authentication(authWithRole("WORKER")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
            .andExpect(status().isForbidden());

        verifyNoInteractions(productCategoryService);
    }

    @Test
    @DisplayName("returns 403 when update is unauthenticated")
    void updateProductCategory_unauthenticated() throws Exception {
        mockMvc.perform(patch("/product-categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
            .andExpect(status().isForbidden());

        verifyNoInteractions(productCategoryService);
    }
}
