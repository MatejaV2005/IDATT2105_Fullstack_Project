package com.grimni.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimni.controller.DeviationController;
import com.grimni.domain.Deviation;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.DeviationCategory;
import com.grimni.domain.enums.ReviewStatus;
import com.grimni.dto.CreateDeviationRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.DeviationService;
import org.springframework.context.annotation.Import;
import com.grimni.security.SecurityConfig;
import com.grimni.util.JwtUtil;
import com.grimni.util.JwtAuthFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviationController.class)
@Import(SecurityConfig.class)
public class DeviationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviationService deviationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private Deviation createDeviation(Long id) {
        Deviation deviation = new Deviation();
        ReflectionTestUtils.setField(deviation, "id", id);
        deviation.setWhatWentWrong("Something went wrong");
        deviation.setImmediateActionTaken("Immediate action taken");
        deviation.setPotentialCause("Potential cause");
        deviation.setPotentialPreventativeMeasure("Preventative measure");
        deviation.setPreventativeMeasureActuallyTaken("");
        deviation.setCategory(DeviationCategory.OTHER);
        deviation.setReviewStatus(ReviewStatus.OPEN);
        return deviation;
    }

    // -------------------------------------------------------------------------
    // POST /deviations
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /deviations")
    class CreateDeviation {

        @Test
        @DisplayName("Should create deviation when request is valid")
        void createDeviation_validRequest_returnsCreated() throws Exception {
            CreateDeviationRequest request = new CreateDeviationRequest(
                    1L, null, null, DeviationCategory.OTHER,
                    "What went wrong",
                    "Immediate action",
                    "Potential cause",
                    "Preventative measure"
            );

            Deviation savedDeviation = createDeviation(1L);
            Organization org = new Organization();
            org.setId(1L);
            savedDeviation.setOrganization(org);

            User reporter = new User();
            reporter.setId(1L);
            reporter.setLegalName("Test User");
            savedDeviation.setReportedBy(reporter);
            ReflectionTestUtils.setField(savedDeviation, "createdAt", LocalDateTime.now());

            when(deviationService.createDeviation(any(CreateDeviationRequest.class), eq(1L)))
                    .thenReturn(savedDeviation);

            mockMvc.perform(post("/deviations")
                            .with(authentication(authWithRole("USER")))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.whatWentWrong").value("Something went wrong"));
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void createDeviation_missingFields_returnsBadRequest() throws Exception {
            String invalidRequest = """
                    {
                        "organizationId": 1,
                        "category": "OTHER"
                    }
                    """;

            mockMvc.perform(post("/deviations")
                            .with(authentication(authWithRole("USER")))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 when not authenticated")
        void createDeviation_unauthenticated_returnsForbidden() throws Exception {
            CreateDeviationRequest request = new CreateDeviationRequest(
                    1L, null, null, DeviationCategory.OTHER,
                    "What went wrong",
                    "Immediate action",
                    "Potential cause",
                    "Preventative measure"
            );

            mockMvc.perform(post("/deviations")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // -------------------------------------------------------------------------
    // GET /deviations
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /deviations")
    class GetAllDeviations {

        @Test
        @DisplayName("Should return list of deviations when authenticated")
        void getAllDeviations_authenticated_returnsList() throws Exception {
            Deviation deviation1 = createDeviation(1L);
            Deviation deviation2 = createDeviation(2L);

            Organization org = new Organization();
            org.setId(1L);
            deviation1.setOrganization(org);
            deviation2.setOrganization(org);

            User reporter = new User();
            reporter.setId(1L);
            reporter.setLegalName("Test User");
            deviation1.setReportedBy(reporter);
            deviation2.setReportedBy(reporter);
            ReflectionTestUtils.setField(deviation1, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(deviation2, "createdAt", LocalDateTime.now());

            when(deviationService.getAllDeviations())
                    .thenReturn(List.of(deviation1, deviation2));

            mockMvc.perform(get("/deviations")
                            .with(authentication(authWithRole("USER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));
        }

        @Test
        @DisplayName("Should return 403 when not authenticated")
        void getAllDeviations_unauthenticated_returnsForbidden() throws Exception {
            mockMvc.perform(get("/deviations"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return empty list when no deviations exist")
        void getAllDeviations_noDeviations_returnsEmptyList() throws Exception {
            when(deviationService.getAllDeviations())
                    .thenReturn(List.of());

            mockMvc.perform(get("/deviations")
                            .with(authentication(authWithRole("USER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // -------------------------------------------------------------------------
    // PATCH /deviations/{id}/resolve
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PATCH /deviations/{id}/resolve")
    class ResolveDeviation {

        @Test
        @DisplayName("Should resolve deviation when request is valid")
        void resolveDeviation_validRequest_returnsResolved() throws Exception {
            Deviation resolvedDeviation = createDeviation(1L);
            resolvedDeviation.setReviewStatus(ReviewStatus.CLOSED);
            resolvedDeviation.setPreventativeMeasureActuallyTaken("Actual preventative measure");

            Organization org = new Organization();
            org.setId(1L);
            resolvedDeviation.setOrganization(org);

            User reviewer = new User();
            reviewer.setId(1L);
            reviewer.setLegalName("Reviewer");
            resolvedDeviation.setReviewedBy(reviewer);
            resolvedDeviation.setReviewedAt(LocalDateTime.now());

            User reporter = new User();
            reporter.setId(2L);
            reporter.setLegalName("Reporter");
            resolvedDeviation.setReportedBy(reporter);
            ReflectionTestUtils.setField(resolvedDeviation, "createdAt", LocalDateTime.now());

            when(deviationService.resolveDeviation(eq(1L), eq(1L), eq("Actual preventative measure")))
                    .thenReturn(resolvedDeviation);

            String requestBody = """
                    {
                        "preventativeMeasureActuallyTaken": "Actual preventative measure"
                    }
                    """;

            mockMvc.perform(patch("/deviations/1/resolve")
                            .with(authentication(authWithRole("USER")))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.reviewStatus").value("CLOSED"))
                    .andExpect(jsonPath("$.preventativeMeasureActuallyTaken").value("Actual preventative measure"));
        }

        @Test
        @DisplayName("Should return 404 when deviation not found")
        void resolveDeviation_notFound_returnsNotFound() throws Exception {
            when(deviationService.resolveDeviation(eq(999L), eq(1L), any()))
                    .thenThrow(new EntityNotFoundException("Deviation not found"));

            String requestBody = """
                    {
                        "preventativeMeasureActuallyTaken": "Some measure"
                    }
                    """;

            mockMvc.perform(patch("/deviations/999/resolve")
                            .with(authentication(authWithRole("USER")))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Deviation not found"));
        }

        @Test
        @DisplayName("Should return 403 when not authenticated")
        void resolveDeviation_unauthenticated_returnsForbidden() throws Exception {
            String requestBody = """
                    {
                        "preventativeMeasureActuallyTaken": "Some measure"
                    }
                    """;

            mockMvc.perform(patch("/deviations/1/resolve")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isForbidden());
        }
    }
}