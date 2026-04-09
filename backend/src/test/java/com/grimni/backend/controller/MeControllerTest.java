package com.grimni.backend.controller;

import com.grimni.controller.MeController;
import com.grimni.domain.Organization;
import com.grimni.dto.AssignedRoutineResponse;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.security.SecurityConfig;
import com.grimni.service.CertificateService;
import com.grimni.service.OrganizationService;
import com.grimni.service.RoutineLoggingService;
import com.grimni.service.UserService;
import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeController.class)
@Import(SecurityConfig.class)
public class MeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private CertificateService certificateService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RoutineLoggingService routineLoggingService;

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

    private Organization createOrg(Long id, String name) {
        Organization org = new Organization();
        ReflectionTestUtils.setField(org, "id", id);
        org.setOrgName(name);
        org.setOrgAddress("123 Main St");
        org.setOrgNumber(100);
        org.setAlcoholEnabled(false);
        org.setFoodEnabled(true);
        return org;
    }

    // -------------------------------------------------------------------------
    // GET /me/organizations — success
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /me/organizations — success")
    class GetMyOrganizationsSuccessTests {

        @Test
        @DisplayName("returns HTTP 200 and list of organization responses")
        void getMyOrganizations_success() throws Exception {
            Organization org1 = createOrg(1L, "Org A");
            Organization org2 = createOrg(2L, "Org B");

            when(organizationService.findOrganizationsByUserId(1L)).thenReturn(List.of(org1, org2));

            mockMvc.perform(get("/me/organizations")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].orgName").value("Org A"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].orgName").value("Org B"));
        }

        @Test
        @DisplayName("returns HTTP 200 and empty list when user has no organizations")
        void getMyOrganizations_empty() throws Exception {
            when(organizationService.findOrganizationsByUserId(1L)).thenReturn(List.of());

            mockMvc.perform(get("/me/organizations")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // GET /me/organizations — failure
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /me/organizations — failure")
    class GetMyOrganizationsFailureTests {

        @Test
        @DisplayName("returns HTTP 404 when service throws EntityNotFoundException")
        void getMyOrganizations_serviceThrows_returns404() throws Exception {
            when(organizationService.findOrganizationsByUserId(1L))
                    .thenThrow(new EntityNotFoundException("Not found"));

            mockMvc.perform(get("/me/organizations")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not found"));
        }

        @Test
        @DisplayName("returns HTTP 403 when unauthenticated")
        void getMyOrganizations_unauthenticated_returns403() throws Exception {
            mockMvc.perform(get("/me/organizations"))
                    .andExpect(status().isForbidden());

            verify(organizationService, never()).findOrganizationsByUserId(any());
        }
    }

    @Nested
    @DisplayName("GET /me/routines")
    class GetMyRoutinesTests {

        @Test
        @DisplayName("returns assigned routines for the active user")
        void getMyRoutines_success() throws Exception {
            LocalDateTime dueAt = LocalDateTime.of(2026, 4, 10, 10, 0);
            LocalDateTime lastCompletedAt = LocalDateTime.of(2026, 4, 9, 9, 15);

            when(routineLoggingService.getAssignedRoutines(1L, 10L)).thenReturn(List.of(
                new AssignedRoutineResponse(
                    17L,
                    "Vask gulvene",
                    "Renhold av lokaler og utstyr",
                    "Vask gulvene etter stengetid.",
                    "Kontakt skiftleder hvis gulvvask ikke kan gjennomfores.",
                    dueAt,
                    false,
                    null,
                    lastCompletedAt
                )
            ));

            mockMvc.perform(get("/me/routines")
                    .with(authentication(authWithRole("WORKER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routineId").value(17))
                .andExpect(jsonPath("$[0].title").value("Vask gulvene"))
                .andExpect(jsonPath("$[0].description").value("Vask gulvene etter stengetid."))
                .andExpect(jsonPath("$[0].completedForCurrentInterval").value(false));
        }
    }

    @Nested
    @DisplayName("POST /me/routines/{routineId}/records")
    class CompleteRoutineTests {

        @Test
        @DisplayName("returns the updated assigned routine response after completion")
        void completeRoutine_success() throws Exception {
            LocalDateTime dueAt = LocalDateTime.of(2026, 4, 10, 10, 0);
            LocalDateTime completedAt = LocalDateTime.of(2026, 4, 10, 8, 5);

            when(routineLoggingService.completeRoutine(17L, 1L, 10L)).thenReturn(
                new AssignedRoutineResponse(
                    17L,
                    "Vask gulvene",
                    "Renhold av lokaler og utstyr",
                    "Vask gulvene etter stengetid.",
                    "Kontakt skiftleder hvis gulvvask ikke kan gjennomfores.",
                    dueAt,
                    true,
                    completedAt,
                    completedAt
                )
            );

            mockMvc.perform(post("/me/routines/17/records")
                    .with(authentication(authWithRole("WORKER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routineId").value(17))
                .andExpect(jsonPath("$.completedForCurrentInterval").value(true));
        }

        @Test
        @DisplayName("returns HTTP 403 when unauthenticated")
        void completeRoutine_unauthenticated_returns403() throws Exception {
            mockMvc.perform(post("/me/routines/17/records"))
                .andExpect(status().isForbidden());

            verify(routineLoggingService, never()).completeRoutine(any(), any(), any());
        }
    }
}
