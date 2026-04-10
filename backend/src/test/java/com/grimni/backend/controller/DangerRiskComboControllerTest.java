package com.grimni.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.grimni.controller.DangerRiskComboController;
import com.grimni.dto.UpdateDangerRiskComboRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.security.SecurityConfig;
import com.grimni.service.DangerRiskComboService;
import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(DangerRiskComboController.class)
@Import(SecurityConfig.class)
public class DangerRiskComboControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DangerRiskComboService dangerRiskComboService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest req = invocation.getArgument(0);
            HttpServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
    }

    private static UsernamePasswordAuthenticationToken authWithRole(String role) {
        JwtUserPrinciple principal = new JwtUserPrinciple(1L, 10L, "alice", role);
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role)));
    }

    private static final String VALID_BODY = """
            {
                "productCategoryId": 5,
                "dangerRiskComboId": 7,
                "danger": "New danger",
                "dangerCorrectiveMeasure": "New corrective measure",
                "severityScore": 4,
                "likelihoodScore": 3
            }
            """;

    @Nested
    @DisplayName("PATCH /danger-risk-combos")
    class UpdateDangerRiskCombo {

        @Test
        @DisplayName("returns 200 OK and invokes the service when caller is OWNER")
        void update_success_asOwner() throws Exception {
            doNothing().when(dangerRiskComboService)
                    .updateDangerRiskCombo(any(UpdateDangerRiskComboRequest.class), eq(10L));

            mockMvc.perform(patch("/danger-risk-combos")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isOk());

            verify(dangerRiskComboService)
                    .updateDangerRiskCombo(any(UpdateDangerRiskComboRequest.class), eq(10L));
        }

        @Test
        @DisplayName("returns 200 OK when caller is MANAGER")
        void update_success_asManager() throws Exception {
            doNothing().when(dangerRiskComboService)
                    .updateDangerRiskCombo(any(UpdateDangerRiskComboRequest.class), eq(10L));

            mockMvc.perform(patch("/danger-risk-combos")
                            .with(authentication(authWithRole("MANAGER")))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 404 when the combo does not exist within the organization")
        void update_notFound() throws Exception {
            doThrow(new EntityNotFoundException("Danger/risk combo not found"))
                    .when(dangerRiskComboService)
                    .updateDangerRiskCombo(any(UpdateDangerRiskComboRequest.class), eq(10L));

            mockMvc.perform(patch("/danger-risk-combos")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 403 when the productCategoryId does not match the combo's category")
        void update_categoryMismatch() throws Exception {
            doThrow(new AccessDeniedException("Danger/risk combo does not belong to the given product category"))
                    .when(dangerRiskComboService)
                    .updateDangerRiskCombo(any(UpdateDangerRiskComboRequest.class), eq(10L));

            mockMvc.perform(patch("/danger-risk-combos")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("returns 400 when required fields are missing")
        void update_validationFailure() throws Exception {
            String invalidBody = """
                    {
                        "productCategoryId": 5,
                        "dangerRiskComboId": 7,
                        "danger": "",
                        "dangerCorrectiveMeasure": "",
                        "severityScore": null,
                        "likelihoodScore": null
                    }
                    """;

            mockMvc.perform(patch("/danger-risk-combos")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(dangerRiskComboService);
        }

        @Test
        @DisplayName("returns 403 when caller is WORKER")
        void update_forbidden_asWorker() throws Exception {
            mockMvc.perform(patch("/danger-risk-combos")
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(dangerRiskComboService);
        }

        @Test
        @DisplayName("returns 403 when unauthenticated")
        void update_unauthenticated() throws Exception {
            mockMvc.perform(patch("/danger-risk-combos")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(dangerRiskComboService);
        }
    }
}
