package com.grimni.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.grimni.controller.InternalControlReviewController;
import com.grimni.domain.InternalControlReview;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.security.SecurityConfig;
import com.grimni.service.InternalControlReviewService;
import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(InternalControlReviewController.class)
@Import(SecurityConfig.class)
public class InternalControlReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InternalControlReviewService internalControlReviewService;

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
        JwtUserPrinciple principal = new JwtUserPrinciple(1L, 10L, "manager", role);
        return new UsernamePasswordAuthenticationToken(
            principal, null, List.of(new SimpleGrantedAuthority(role))
        );
    }

    @Nested
    @DisplayName("GET /internal-control-reviews")
    class GetReviewsTests {

        @Test
        @DisplayName("returns reviews for manager")
        void getReviews_success() throws Exception {
            InternalControlReview newer = createReview(2L, "Newest summary", LocalDateTime.now());
            InternalControlReview older = createReview(1L, "Older summary", LocalDateTime.now().minusDays(1));

            when(internalControlReviewService.getReviews(1L, 10L)).thenReturn(List.of(newer, older));

            mockMvc.perform(get("/internal-control-reviews")
                    .with(authentication(authWithRole("MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].summary").value("Newest summary"))
                .andExpect(jsonPath("$[1].id").value(1));
        }

        @Test
        @DisplayName("rejects worker access")
        void getReviews_workerForbidden() throws Exception {
            mockMvc.perform(get("/internal-control-reviews")
                    .with(authentication(authWithRole("WORKER"))))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /internal-control-reviews")
    class CreateReviewTests {

        @Test
        @DisplayName("creates review for owner")
        void createReview_success() throws Exception {
            InternalControlReview review = createReview(5L, "Daily summary", LocalDateTime.now());

            when(internalControlReviewService.createReview(any(), eq(1L), eq(10L))).thenReturn(review);

            mockMvc.perform(post("/internal-control-reviews")
                    .with(authentication(authWithRole("OWNER")))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "summary": "Daily summary"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.organizationId").value(10))
                .andExpect(jsonPath("$.reviewedById").value(1))
                .andExpect(jsonPath("$.reviewedByName").value("Manager User"))
                .andExpect(jsonPath("$.summary").value("Daily summary"));
        }

        @Test
        @DisplayName("rejects blank summary at validation")
        void createReview_blankSummary_returnsBadRequest() throws Exception {
            mockMvc.perform(post("/internal-control-reviews")
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "summary": "   "
                        }
                        """))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 when org membership is missing")
        void createReview_notFound_returns404() throws Exception {
            when(internalControlReviewService.createReview(any(), eq(1L), eq(10L)))
                .thenThrow(new EntityNotFoundException("Organization not found"));

            mockMvc.perform(post("/internal-control-reviews")
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "summary": "Daily summary"
                        }
                        """))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("rejects worker access")
        void createReview_workerForbidden() throws Exception {
            mockMvc.perform(post("/internal-control-reviews")
                    .with(authentication(authWithRole("WORKER")))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "summary": "Daily summary"
                        }
                        """))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /internal-control-reviews/{reviewId}")
    class DeleteReviewTests {

        @Test
        @DisplayName("deletes review")
        void deleteReview_success() throws Exception {
            mockMvc.perform(delete("/internal-control-reviews/7")
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when review is missing")
        void deleteReview_notFound() throws Exception {
            doThrow(new EntityNotFoundException("Internal control review not found"))
                .when(internalControlReviewService).deleteReview(7L, 1L, 10L);

            mockMvc.perform(delete("/internal-control-reviews/7")
                    .with(authentication(authWithRole("OWNER")))
                    .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("rejects worker access")
        void deleteReview_workerForbidden() throws Exception {
            mockMvc.perform(delete("/internal-control-reviews/7")
                    .with(authentication(authWithRole("WORKER")))
                    .with(csrf()))
                .andExpect(status().isForbidden());
        }
    }

    private InternalControlReview createReview(Long id, String summary, LocalDateTime createdAt) {
        Organization organization = new Organization();
        organization.setId(10L);

        User reviewer = new User();
        reviewer.setId(1L);
        reviewer.setLegalName("Manager User");

        InternalControlReview review = new InternalControlReview();
        ReflectionTestUtils.setField(review, "id", id);
        review.setOrganization(organization);
        review.setReviewedBy(reviewer);
        review.setSummary(summary);
        ReflectionTestUtils.setField(review, "createdAt", createdAt);
        return review;
    }
}
