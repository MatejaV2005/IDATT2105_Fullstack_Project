package com.grimni.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimni.controller.OrganizationController;
import com.grimni.domain.Organization;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.dto.AddOrganizationUserRequest;
import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.UpdateOrganizationRequest;
import com.grimni.dto.UserOrgResponse;
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

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationController.class)
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
    // POST /organizations/users — success
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /organizations/users — success")
    class AddOrganizationUserSuccessTests {

        @Test
        @DisplayName("returns HTTP 200 and the added organization user response")
        void addOrganizationUser_success() throws Exception {
            AddOrganizationUserRequest request = new AddOrganizationUserRequest(18L, OrgUserRole.MANAGER);
            UserOrgResponse response = new UserOrgResponse(18L, "New Manager", "manager@example.com", OrgUserRole.MANAGER);

            when(organizationService.addUserToOrganization(eq(10L), eq(1L), any(AddOrganizationUserRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/organizations/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(18))
                    .andExpect(jsonPath("$.legalName").value("New Manager"))
                    .andExpect(jsonPath("$.email").value("manager@example.com"))
                    .andExpect(jsonPath("$.accessLevel").value("MANAGER"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /organizations/users — failure
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /organizations/users — failure")
    class AddOrganizationUserFailureTests {

        @Test
        @DisplayName("returns HTTP 403 when requester is not an owner")
        void addOrganizationUser_managerForbidden() throws Exception {
            AddOrganizationUserRequest request = new AddOrganizationUserRequest(18L, OrgUserRole.MANAGER);

            mockMvc.perform(post("/organizations/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("MANAGER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(organizationService, never()).addUserToOrganization(anyLong(), anyLong(), any());
        }

        @Test
        @DisplayName("returns HTTP 404 when target user does not exist")
        void addOrganizationUser_userNotFound_returns404() throws Exception {
            AddOrganizationUserRequest request = new AddOrganizationUserRequest(18L, OrgUserRole.MANAGER);

            when(organizationService.addUserToOrganization(eq(10L), eq(1L), any(AddOrganizationUserRequest.class)))
                    .thenThrow(new EntityNotFoundException("User not found"));

            mockMvc.perform(post("/organizations/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("User not found"));
        }

        @Test
        @DisplayName("returns HTTP 400 when user already belongs to the organization")
        void addOrganizationUser_duplicate_returns400() throws Exception {
            AddOrganizationUserRequest request = new AddOrganizationUserRequest(18L, OrgUserRole.MANAGER);

            when(organizationService.addUserToOrganization(eq(10L), eq(1L), any(AddOrganizationUserRequest.class)))
                    .thenThrow(new IllegalArgumentException("User is already in the organization"));

            mockMvc.perform(post("/organizations/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("User is already in the organization"));
        }
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
        @DisplayName("returns HTTP 404 when user not found")
        void createOrganization_userNotFound_returns404() throws Exception {
            CreateOrganizationRequest request = new CreateOrganizationRequest("New Org", "123 Main St", 100, false, true);

            when(organizationService.createOrganization(any(CreateOrganizationRequest.class), eq(1L)))
                    .thenThrow(new EntityNotFoundException("User not found"));

            mockMvc.perform(post("/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("User not found"));
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
    // GET /organizations — success
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /organizations — success")
    class GetOrganizationSuccessTests {

        @Test
        @DisplayName("returns HTTP 200 and the organization response")
        void getOrganization_success() throws Exception {
            Organization org = createOrg(10L, "Test Org");

            when(organizationService.findOrganizationByIdAndUser(10L, 1L)).thenReturn(org);

            mockMvc.perform(get("/organizations")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.orgName").value("Test Org"))
                    .andExpect(jsonPath("$.orgNumber").value(100))
                    .andExpect(jsonPath("$.orgAddress").value("123 Main St"))
                    .andExpect(jsonPath("$.alcoholEnabled").value(false))
                    .andExpect(jsonPath("$.foodEnabled").value(true));
        }

        @Test
        @DisplayName("response does not contain entity collections")
        void getOrganization_responseHasNoCollections() throws Exception {
            Organization org = createOrg(10L, "Test Org");

            when(organizationService.findOrganizationByIdAndUser(10L, 1L)).thenReturn(org);

            mockMvc.perform(get("/organizations")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.todos").doesNotExist())
                    .andExpect(jsonPath("$.members").doesNotExist())
                    .andExpect(jsonPath("$.ccps").doesNotExist())
                    .andExpect(jsonPath("$.deviations").doesNotExist());
        }
    }

    // -------------------------------------------------------------------------
    // GET /organizations — failure
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /organizations — failure")
    class GetOrganizationFailureTests {

        @Test
        @DisplayName("returns HTTP 404 when organization not found")
        void getOrganization_notFound_returns404() throws Exception {
            when(organizationService.findOrganizationByIdAndUser(10L, 1L))
                    .thenThrow(new EntityNotFoundException("Organization not found"));

            mockMvc.perform(get("/organizations")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Organization not found"));
        }

        @Test
        @DisplayName("returns HTTP 403 when unauthenticated")
        void getOrganization_unauthenticated_returns403() throws Exception {
            mockMvc.perform(get("/organizations"))
                    .andExpect(status().isForbidden());

            verify(organizationService, never()).findOrganizationByIdAndUser(any(), any());
        }
    }

    // -------------------------------------------------------------------------
    // PATCH /organizations — success
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PATCH /organizations — success")
    class UpdateOrganizationSuccessTests {

        @Test
        @DisplayName("returns HTTP 200 and the updated organization response")
        void updateOrganization_success() throws Exception {
            Organization updated = createOrg(10L, "Updated Org");
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Updated Org", null, null, null, null);

            when(organizationService.updateOrganization(eq(10L), any(UpdateOrganizationRequest.class), eq(1L)))
                    .thenReturn(updated);

            mockMvc.perform(patch("/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.orgName").value("Updated Org"))
                    .andExpect(jsonPath("$.orgAddress").value("123 Main St"))
                    .andExpect(jsonPath("$.orgNumber").value(100));

            verify(organizationService).updateOrganization(eq(10L), any(UpdateOrganizationRequest.class), eq(1L));
        }
    }

    // -------------------------------------------------------------------------
    // PATCH /organizations — failure
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PATCH /organizations — failure")
    class UpdateOrganizationFailureTests {

        @Test
        @DisplayName("returns HTTP 404 when organization not found")
        void updateOrganization_notFound_returns404() throws Exception {
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Name", "Addr", 100, false, false);

            when(organizationService.updateOrganization(eq(10L), any(UpdateOrganizationRequest.class), eq(1L)))
                    .thenThrow(new EntityNotFoundException("Organization not found"));

            mockMvc.perform(patch("/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Organization not found"));
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

            mockMvc.perform(patch("/organizations")
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
            Organization updated = createOrg(10L, "Updated");
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Updated", null, null, null, null);

            when(organizationService.updateOrganization(eq(10L), any(UpdateOrganizationRequest.class), eq(1L)))
                    .thenReturn(updated);

            mockMvc.perform(patch("/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PATCH — MANAGER role is allowed")
        void updateOrganization_managerRole_isAllowed() throws Exception {
            Organization updated = createOrg(10L, "Updated");
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Updated", null, null, null, null);

            when(organizationService.updateOrganization(eq(10L), any(UpdateOrganizationRequest.class), eq(1L)))
                    .thenReturn(updated);

            mockMvc.perform(patch("/organizations")
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

            mockMvc.perform(patch("/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(organizationService, never()).updateOrganization(any(), any(), any());
        }
    }

    // -------------------------------------------------------------------------
    // GET /organizations/users
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /organizations/users")
    class GetAllUsersInOrgTests {

        @Test
        @DisplayName("returns all users in the organization")
        void getAllUsersInOrg_success() throws Exception {
            when(organizationService.getAllUsersInOrg(10L)).thenReturn(List.of(
                    new UserOrgResponse(1L, "alice", "alice@test.com", OrgUserRole.OWNER),
                    new UserOrgResponse(2L, "bob", "bob@test.com", OrgUserRole.WORKER)
            ));

            mockMvc.perform(get("/organizations/users")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].legalName").value("alice"))
                    .andExpect(jsonPath("$[0].email").value("alice@test.com"))
                    .andExpect(jsonPath("$[0].accessLevel").value("OWNER"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].accessLevel").value("WORKER"));
        }

        @Test
        @DisplayName("returns empty list when no users")
        void getAllUsersInOrg_empty() throws Exception {
            when(organizationService.getAllUsersInOrg(10L)).thenReturn(List.of());

            mockMvc.perform(get("/organizations/users")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("unauthenticated returns 403")
        void getAllUsersInOrg_unauthenticated() throws Exception {
            mockMvc.perform(get("/organizations/users"))
                    .andExpect(status().isForbidden());

            verify(organizationService, never()).getAllUsersInOrg(anyLong());
        }
    }
}
