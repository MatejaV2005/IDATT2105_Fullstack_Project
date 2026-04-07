package com.grimni.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimni.controller.OrganizationController;
import com.grimni.controller.MeController;
import com.grimni.domain.Organization;
import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.UpdateOrganizationRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.OrganizationService;
import org.springframework.context.annotation.Import;
import com.grimni.security.SecurityConfig;
import com.grimni.util.JwtUtil;

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

import com.grimni.util.JwtAuthFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({OrganizationController.class, MeController.class})
@Import(SecurityConfig.class)
public class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationService organizationService;

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

    private static UsernamePasswordAuthenticationToken authWithRole(String role, Long userId, Long orgId) {
        JwtUserPrinciple principal = new JwtUserPrinciple(userId, orgId, "alice", role);
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
    // POST /organizations — success
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /organizations — success")
    class CreateOrganizationSuccessTests {

        @Test
        @DisplayName("returns HTTP 201 and the created organization response")
        void createOrganization_success() throws Exception {
            Organization org = createOrg(1L, "New Org");
            CreateOrganizationRequest request = new CreateOrganizationRequest("New Org", "123 Main St", 100, false, true);

            when(organizationService.createOrganization(any(CreateOrganizationRequest.class), eq(1L))).thenReturn(org);

            mockMvc.perform(post("/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.orgName").value("New Org"))
                    .andExpect(jsonPath("$.orgAddress").value("123 Main St"))
                    .andExpect(jsonPath("$.orgNumber").value(100))
                    .andExpect(jsonPath("$.alcoholEnabled").value(false))
                    .andExpect(jsonPath("$.foodEnabled").value(true));

            verify(organizationService).createOrganization(any(CreateOrganizationRequest.class), eq(1L));
        }

        @Test
        @DisplayName("any authenticated user can create an organization")
        void createOrganization_workerCanCreate() throws Exception {
            Organization org = createOrg(2L, "Worker Org");
            CreateOrganizationRequest request = new CreateOrganizationRequest("Worker Org", "456 St", 200, true, false);

            when(organizationService.createOrganization(any(CreateOrganizationRequest.class), eq(1L))).thenReturn(org);

            mockMvc.perform(post("/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isCreated());
        }
    }

    // -------------------------------------------------------------------------
    // POST /organizations — failure
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /organizations — failure")
    class CreateOrganizationFailureTests {

        @Test
        @DisplayName("returns HTTP 400 when user not found")
        void createOrganization_userNotFound_returns400() throws Exception {
            CreateOrganizationRequest request = new CreateOrganizationRequest("New Org", "123 Main St", 100, false, true);

            when(organizationService.createOrganization(any(CreateOrganizationRequest.class), eq(1L)))
                    .thenThrow(new RuntimeException("User not found"));

            mockMvc.perform(post("/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("User not found"));
        }

        @Test
        @DisplayName("returns HTTP 403 when unauthenticated")
        void createOrganization_unauthenticated_returns403() throws Exception {
            CreateOrganizationRequest request = new CreateOrganizationRequest("New Org", "123 Main St", 100, false, true);

            mockMvc.perform(post("/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(organizationService, never()).createOrganization(any(), any());
        }
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
        @DisplayName("returns HTTP 500 when service throws")
        void getMyOrganizations_serviceThrows_returns500() throws Exception {
            when(organizationService.findOrganizationsByUserId(1L))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/me/organizations")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Database error"));
        }

        @Test
        @DisplayName("returns HTTP 403 when unauthenticated")
        void getMyOrganizations_unauthenticated_returns403() throws Exception {
            mockMvc.perform(get("/me/organizations"))
                    .andExpect(status().isForbidden());

            verify(organizationService, never()).findOrganizationsByUserId(any());
        }
    }

    // -------------------------------------------------------------------------
    // GET /organizations/{orgId} — success
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /organizations/{orgId} — success")
    class GetOrganizationSuccessTests {

        @Test
        @DisplayName("returns HTTP 200 and the organization response")
        void getOrganization_success() throws Exception {
            Organization org = createOrg(1L, "Test Org");

            when(organizationService.findOrganizationByIdAndUser(1L, 1L)).thenReturn(org);

            mockMvc.perform(get("/organizations/1")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.orgName").value("Test Org"))
                    .andExpect(jsonPath("$.orgNumber").value(100))
                    .andExpect(jsonPath("$.orgAddress").value("123 Main St"))
                    .andExpect(jsonPath("$.alcoholEnabled").value(false))
                    .andExpect(jsonPath("$.foodEnabled").value(true));
        }

        @Test
        @DisplayName("response does not contain entity collections")
        void getOrganization_responseHasNoCollections() throws Exception {
            Organization org = createOrg(1L, "Test Org");

            when(organizationService.findOrganizationByIdAndUser(1L, 1L)).thenReturn(org);

            mockMvc.perform(get("/organizations/1")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.todos").doesNotExist())
                    .andExpect(jsonPath("$.members").doesNotExist())
                    .andExpect(jsonPath("$.ccps").doesNotExist())
                    .andExpect(jsonPath("$.deviations").doesNotExist());
        }
    }

    // -------------------------------------------------------------------------
    // GET /organizations/{orgId} — failure
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /organizations/{orgId} — failure")
    class GetOrganizationFailureTests {

        @Test
        @DisplayName("returns HTTP 404 when organization not found")
        void getOrganization_notFound_returns404() throws Exception {
            when(organizationService.findOrganizationByIdAndUser(999L, 1L))
                    .thenThrow(new RuntimeException("Organization not found"));

            mockMvc.perform(get("/organizations/999")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Organization not found"));
        }

        @Test
        @DisplayName("returns HTTP 404 when user does not belong to organization")
        void getOrganization_notMember_returns404() throws Exception {
            when(organizationService.findOrganizationByIdAndUser(99L, 1L))
                    .thenThrow(new RuntimeException("Organization not found"));

            mockMvc.perform(get("/organizations/99")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Organization not found"));

            verify(organizationService).findOrganizationByIdAndUser(99L, 1L);
        }

        @Test
        @DisplayName("returns HTTP 403 when unauthenticated")
        void getOrganization_unauthenticated_returns403() throws Exception {
            mockMvc.perform(get("/organizations/1"))
                    .andExpect(status().isForbidden());

            verify(organizationService, never()).findOrganizationByIdAndUser(any(), any());
        }
    }

    // -------------------------------------------------------------------------
    // PATCH /organizations/{orgId} — success
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PATCH /organizations/{orgId} — success")
    class UpdateOrganizationSuccessTests {

        @Test
        @DisplayName("returns HTTP 200 and the updated organization response")
        void updateOrganization_success() throws Exception {
            Organization updated = createOrg(1L, "Updated Org");
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Updated Org", null, null, null, null);

            when(organizationService.updateOrganization(eq(1L), any(UpdateOrganizationRequest.class), eq(1L)))
                    .thenReturn(updated);

            mockMvc.perform(patch("/organizations/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.orgName").value("Updated Org"))
                    .andExpect(jsonPath("$.orgAddress").value("123 Main St"))
                    .andExpect(jsonPath("$.orgNumber").value(100));

            verify(organizationService).updateOrganization(eq(1L), any(UpdateOrganizationRequest.class), eq(1L));
        }
    }

    // -------------------------------------------------------------------------
    // PATCH /organizations/{orgId} — failure
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PATCH /organizations/{orgId} — failure")
    class UpdateOrganizationFailureTests {

        @Test
        @DisplayName("returns HTTP 400 when organization not found")
        void updateOrganization_notFound_returns400() throws Exception {
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Name", "Addr", 100, false, false);

            when(organizationService.updateOrganization(eq(999L), any(UpdateOrganizationRequest.class), eq(1L)))
                    .thenThrow(new RuntimeException("Organization not found"));

            mockMvc.perform(patch("/organizations/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Organization not found"));
        }

        @Test
        @DisplayName("returns HTTP 400 when user does not belong to organization")
        void updateOrganization_notMember_returns400() throws Exception {
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Name", null, null, null, null);

            when(organizationService.updateOrganization(eq(99L), any(UpdateOrganizationRequest.class), eq(1L)))
                    .thenThrow(new RuntimeException("Organization not found"));

            mockMvc.perform(patch("/organizations/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Organization not found"));

            verify(organizationService).updateOrganization(eq(99L), any(UpdateOrganizationRequest.class), eq(1L));
        }
    }

    // =========================================================================
    // AUTHORIZATION TESTS — @PreAuthorize enforcement
    // =========================================================================
    @Nested
    @DisplayName("Authorization — @PreAuthorize enforcement")
    class AuthorizationTests {

        @Test
        @DisplayName("PATCH — WORKER role is rejected with 403")
        void updateOrganization_workerRole_returns403() throws Exception {
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Name", "Addr", 100, false, false);

            mockMvc.perform(patch("/organizations/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(organizationService, never()).updateOrganization(any(), any(), any());
        }

        @Test
        @DisplayName("PATCH — OWNER role is allowed")
        void updateOrganization_ownerRole_isAllowed() throws Exception {
            Organization updated = createOrg(1L, "Updated");
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Updated", null, null, null, null);

            when(organizationService.updateOrganization(eq(1L), any(UpdateOrganizationRequest.class), eq(1L)))
                    .thenReturn(updated);

            mockMvc.perform(patch("/organizations/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PATCH — MANAGER role is allowed")
        void updateOrganization_managerRole_isAllowed() throws Exception {
            Organization updated = createOrg(1L, "Updated");
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Updated", null, null, null, null);

            when(organizationService.updateOrganization(eq(1L), any(UpdateOrganizationRequest.class), eq(1L)))
                    .thenReturn(updated);

            mockMvc.perform(patch("/organizations/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("MANAGER")))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PATCH — unauthenticated request is rejected with 403")
        void updateOrganization_unauthenticated_returns403() throws Exception {
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Name", "Addr", 100, false, false);

            mockMvc.perform(patch("/organizations/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(organizationService, never()).updateOrganization(any(), any(), any());
        }
    }
}