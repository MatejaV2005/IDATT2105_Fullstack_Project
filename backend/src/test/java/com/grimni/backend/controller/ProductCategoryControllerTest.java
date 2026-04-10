package com.grimni.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.grimni.dto.ProductCategoryResponse;
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
                new ProductCategoryResponse(1L, "Fisk og sjomat"),
                new ProductCategoryResponse(2L, "Kjott og farseprodukter")
        ));

        mockMvc.perform(get("/product-categories")
                .with(authentication(authWithRole("WORKER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].productDescription").value("Fisk og sjomat"))
            .andExpect(jsonPath("$[1].id").value(2))
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
}
