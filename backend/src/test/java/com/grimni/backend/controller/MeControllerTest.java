package com.grimni.backend.controller;

import com.grimni.controller.MeController;
import com.grimni.domain.Deviation;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.DeviationCategory;
import com.grimni.domain.enums.ReviewStatus;
import com.grimni.dto.AssignedCcpResponse;
import com.grimni.dto.AssignedRoutineResponse;
import com.grimni.dto.MyOrganizationMembershipResponse;
import com.grimni.dto.SubmittedCcpRecordResponse;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.security.SecurityConfig;
import com.grimni.service.CcpLoggingService;
import com.grimni.service.CertificateService;
import com.grimni.service.DeviationService;
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
    private CcpLoggingService ccpLoggingService;

    @MockitoBean
    private DeviationService deviationService;

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
        @DisplayName("returns HTTP 200 and list of membership-aware organization responses")
        void getMyOrganizations_success() throws Exception {
            when(organizationService.getMyOrganizationMemberships(1L, 10L)).thenReturn(List.of(
                new MyOrganizationMembershipResponse(1L, "Org A", "123 Main St", 100, false, true, "OWNER", false),
                new MyOrganizationMembershipResponse(10L, "Org B", "123 Main St", 100, false, true, "WORKER", true)
            ));

            mockMvc.perform(get("/me/organizations")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].orgName").value("Org A"))
                    .andExpect(jsonPath("$[0].orgRole").value("OWNER"))
                    .andExpect(jsonPath("$[0].isCurrent").value(false))
                    .andExpect(jsonPath("$[1].id").value(10))
                    .andExpect(jsonPath("$[1].orgName").value("Org B"))
                    .andExpect(jsonPath("$[1].orgRole").value("WORKER"))
                    .andExpect(jsonPath("$[1].isCurrent").value(true));
        }

        @Test
        @DisplayName("returns HTTP 200 and empty list when user has no organizations")
        void getMyOrganizations_empty() throws Exception {
            when(organizationService.getMyOrganizationMemberships(1L, 10L)).thenReturn(List.of());

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
            when(organizationService.getMyOrganizationMemberships(1L, 10L))
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

            verify(organizationService, never()).getMyOrganizationMemberships(any(), any());
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

    @Nested
    @DisplayName("GET /me/ccps")
    class GetMyCcpsTests {

        @Test
        @DisplayName("returns assigned critical control points for the active user")
        void getMyCcps_success() throws Exception {
            LocalDateTime dueAt = LocalDateTime.of(2026, 4, 10, 11, 0);
            LocalDateTime lastCompletedAt = LocalDateTime.of(2026, 4, 9, 8, 30);

            when(ccpLoggingService.getAssignedCcps(1L, 10L)).thenReturn(List.of(
                new AssignedCcpResponse(
                    41L,
                    "Kjolerom",
                    "Temperatur i kjolerom",
                    java.math.BigDecimal.ZERO,
                    java.math.BigDecimal.valueOf(4),
                    "C",
                    "Starts 2026-04-09 10:00, repeats every 1 day",
                    dueAt,
                    false,
                    null,
                    lastCompletedAt,
                    java.math.BigDecimal.valueOf(3.5),
                    "Flytt varer til reservekjol."
                )
            ));

            mockMvc.perform(get("/me/ccps")
                    .with(authentication(authWithRole("WORKER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ccpId").value(41))
                .andExpect(jsonPath("$[0].name").value("Kjolerom"))
                .andExpect(jsonPath("$[0].completedForCurrentInterval").value(false));
        }
    }

    @Nested
    @DisplayName("POST /me/ccps/{ccpId}/records")
    class CreateCcpRecordTests {

        @Test
        @DisplayName("returns the updated assigned CCP response after submission")
        void createCcpRecord_success() throws Exception {
            AssignedCcpResponse response = new AssignedCcpResponse(
                41L,
                "Kjolerom",
                "Temperatur i kjolerom",
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.valueOf(4),
                "C",
                "Starts 2026-04-09 10:00, repeats every 1 day",
                LocalDateTime.of(2026, 4, 10, 11, 0),
                true,
                LocalDateTime.of(2026, 4, 10, 10, 15),
                LocalDateTime.of(2026, 4, 10, 10, 15),
                java.math.BigDecimal.valueOf(5.5),
                "Flytt varer til reservekjol."
            );

            when(ccpLoggingService.createRecord(eq(41L), any(), eq(1L), eq(10L))).thenReturn(
                new SubmittedCcpRecordResponse(response, 88L, true)
            );

            mockMvc.perform(post("/me/ccps/41/records")
                    .with(authentication(authWithRole("WORKER")))
                    .contentType("application/json")
                    .content("""
                        {
                          "measuredValue": 5.5,
                          "comment": "For hoy verdi"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(88))
                .andExpect(jsonPath("$.outsideCriticalRange").value(true))
                .andExpect(jsonPath("$.ccp.ccpId").value(41))
                .andExpect(jsonPath("$.ccp.completedForCurrentInterval").value(true));
        }
    }

    @Nested
    @DisplayName("POST /me/deviations")
    class CreateMyDeviationTests {

        @Test
        @DisplayName("creates a deviation using the authenticated user's organization")
        void createMyDeviation_success() throws Exception {
            Deviation deviation = new Deviation();
            ReflectionTestUtils.setField(deviation, "id", 501L);
            deviation.setCategory(DeviationCategory.IK_MAT);
            deviation.setReviewStatus(ReviewStatus.OPEN);
            deviation.setWhatWentWrong("Temperaturen var for hoy");
            deviation.setImmediateActionTaken("Flyttet varene");
            deviation.setPotentialCause("Feil pa kjolen");
            deviation.setPotentialPreventativeMeasure("Sjekk kjolen daglig");

            Organization organization = createOrg(10L, "Org A");
            deviation.setOrganization(organization);

            User reporter = new User();
            reporter.setId(1L);
            reporter.setLegalName("Alice");
            deviation.setReportedBy(reporter);
            ReflectionTestUtils.setField(deviation, "createdAt", LocalDateTime.now());

            when(deviationService.createDeviation(any(), eq(1L))).thenReturn(deviation);

            mockMvc.perform(post("/me/deviations")
                    .with(authentication(authWithRole("WORKER")))
                    .contentType("application/json")
                    .content("""
                        {
                          "ccpRecordId": 88,
                          "category": "IK_MAT",
                          "whatWentWrong": "Temperaturen var for hoy",
                          "immediateActionTaken": "Flyttet varene",
                          "potentialCause": "Feil pa kjolen",
                          "potentialPreventativeMeasure": "Sjekk kjolen daglig"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(501))
                .andExpect(jsonPath("$.organizationId").value(10))
                .andExpect(jsonPath("$.category").value("IK_MAT"));
        }
    }
}
