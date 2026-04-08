package com.grimni.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimni.controller.CertificationController;
import com.grimni.domain.Certificate;
import com.grimni.domain.Course;
import com.grimni.domain.FileObject;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.dto.CreateCertificateRequest;
import com.grimni.dto.UpdateCertificateRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.security.SecurityConfig;
import com.grimni.service.CertificateService;
import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

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

import jakarta.persistence.EntityNotFoundException;
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

@WebMvcTest(CertificationController.class)
@Import(SecurityConfig.class)
public class CertificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CertificateService certificateService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Organization testOrg;
    private User targetUser;
    private FileObject testFile;
    private Course testCourse;
    private Certificate testCert;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest req = invocation.getArgument(0);
            HttpServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));

        testOrg = new Organization();
        ReflectionTestUtils.setField(testOrg, "id", 10L);
        testOrg.setOrgName("Test Org");

        targetUser = new User();
        ReflectionTestUtils.setField(targetUser, "id", 2L);
        targetUser.setLegalName("bob");

        testFile = new FileObject();
        ReflectionTestUtils.setField(testFile, "id", 50L);

        testCourse = new Course();
        testCourse.setId(100L);
        testCourse.setTitle("Safety Course");

        testCert = new Certificate();
        testCert.setId(200L);
        testCert.setCertificateName("Safety Cert");
        testCert.setUser(targetUser);
        testCert.setFile(testFile);
        testCert.setOrganization(testOrg);
        testCert.setCourse(testCourse);
    }

    private static UsernamePasswordAuthenticationToken authWithRole(String role) {
        JwtUserPrinciple principal = new JwtUserPrinciple(1L, 10L, "alice", role);
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role)));
    }

    // =========================================================================
    // POST /certificates
    // =========================================================================

    @Nested
    @DisplayName("POST /certificates")
    class CreateCertificateTests {

        @Test
        @DisplayName("OWNER can create — returns 201 with response body")
        void createCertificate_owner_returns201() throws Exception {
            CreateCertificateRequest request = new CreateCertificateRequest("Safety Cert", 2L, 50L, 100L);
            when(certificateService.createCertificate(any(CreateCertificateRequest.class), eq(10L), eq(1L)))
                    .thenReturn(testCert);

            mockMvc.perform(post("/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(200))
                    .andExpect(jsonPath("$.certificateName").value("Safety Cert"))
                    .andExpect(jsonPath("$.userId").value(2))
                    .andExpect(jsonPath("$.fileId").value(50))
                    .andExpect(jsonPath("$.organizationId").value(10))
                    .andExpect(jsonPath("$.courseId").value(100));
        }

        @Test
        @DisplayName("MANAGER can create — returns 201")
        void createCertificate_manager_returns201() throws Exception {
            CreateCertificateRequest request = new CreateCertificateRequest("Cert", 2L, 50L, null);
            when(certificateService.createCertificate(any(CreateCertificateRequest.class), eq(10L), eq(1L)))
                    .thenReturn(testCert);

            mockMvc.perform(post("/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("MANAGER")))
                            .with(csrf()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("WORKER cannot create — returns 403")
        void createCertificate_worker_returns403() throws Exception {
            CreateCertificateRequest request = new CreateCertificateRequest("Cert", 2L, 50L, null);

            mockMvc.perform(post("/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(certificateService, never()).createCertificate(any(), any(), any());
        }

        @Test
        @DisplayName("unauthenticated — returns 403")
        void createCertificate_unauthenticated_returns403() throws Exception {
            CreateCertificateRequest request = new CreateCertificateRequest("Cert", 2L, 50L, null);

            mockMvc.perform(post("/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(certificateService, never()).createCertificate(any(), any(), any());
        }

        @Test
        @DisplayName("service throws EntityNotFound — returns 404")
        void createCertificate_notFound_returns404() throws Exception {
            CreateCertificateRequest request = new CreateCertificateRequest("Cert", 2L, 50L, null);
            when(certificateService.createCertificate(any(), eq(10L), eq(1L)))
                    .thenThrow(new EntityNotFoundException("User not found"));

            mockMvc.perform(post("/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("User not found"));
        }
    }

    // =========================================================================
    // GET /certificates
    // =========================================================================

    @Nested
    @DisplayName("GET /certificates")
    class GetOrgCertificatesTests {

        @Test
        @DisplayName("OWNER gets all org certificates — returns 200")
        void getOrgCertificates_owner_returns200() throws Exception {
            when(certificateService.getCertificatesForOrg(10L, 1L)).thenReturn(List.of(testCert));

            mockMvc.perform(get("/certificates")
                            .with(authentication(authWithRole("OWNER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(200))
                    .andExpect(jsonPath("$[0].certificateName").value("Safety Cert"));
        }

        @Test
        @DisplayName("WORKER cannot list org certificates — returns 403")
        void getOrgCertificates_worker_returns403() throws Exception {
            mockMvc.perform(get("/certificates")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isForbidden());

            verify(certificateService, never()).getCertificatesForOrg(any(), any());
        }

        @Test
        @DisplayName("returns empty list when no certificates")
        void getOrgCertificates_empty_returns200() throws Exception {
            when(certificateService.getCertificatesForOrg(10L, 1L)).thenReturn(List.of());

            mockMvc.perform(get("/certificates")
                            .with(authentication(authWithRole("OWNER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // =========================================================================
    // GET /certificates/{certId}
    // =========================================================================

    @Nested
    @DisplayName("GET /certificates/{certId}")
    class GetCertificateByIdTests {

        @Test
        @DisplayName("any authenticated user can view — returns 200")
        void getCertificate_worker_returns200() throws Exception {
            when(certificateService.getCertificateById(200L, 10L, 1L)).thenReturn(testCert);

            mockMvc.perform(get("/certificates/200")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(200))
                    .andExpect(jsonPath("$.certificateName").value("Safety Cert"))
                    .andExpect(jsonPath("$.userId").value(2))
                    .andExpect(jsonPath("$.fileId").value(50))
                    .andExpect(jsonPath("$.organizationId").value(10))
                    .andExpect(jsonPath("$.courseId").value(100));
        }

        @Test
        @DisplayName("returns 404 when not found")
        void getCertificate_notFound_returns404() throws Exception {
            when(certificateService.getCertificateById(999L, 10L, 1L))
                    .thenThrow(new EntityNotFoundException("Certificate not found"));

            mockMvc.perform(get("/certificates/999")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Certificate not found"));
        }

        @Test
        @DisplayName("unauthenticated — returns 403")
        void getCertificate_unauthenticated_returns403() throws Exception {
            mockMvc.perform(get("/certificates/200"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // GET /certificates/user/{targetUserId}
    // =========================================================================

    @Nested
    @DisplayName("GET /certificates/user/{targetUserId}")
    class GetUserCertificatesTests {

        @Test
        @DisplayName("OWNER gets user certificates in org — returns 200")
        void getUserCertificates_owner_returns200() throws Exception {
            when(certificateService.getCertificatesForUserInOrg(2L, 10L, 1L)).thenReturn(List.of(testCert));

            mockMvc.perform(get("/certificates/user/2")
                            .with(authentication(authWithRole("OWNER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(200))
                    .andExpect(jsonPath("$[0].userId").value(2));
        }

        @Test
        @DisplayName("WORKER cannot view other user certificates — returns 403")
        void getUserCertificates_worker_returns403() throws Exception {
            mockMvc.perform(get("/certificates/user/2")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isForbidden());

            verify(certificateService, never()).getCertificatesForUserInOrg(any(), any(), any());
        }

        @Test
        @DisplayName("returns 404 when target user not in org")
        void getUserCertificates_targetNotInOrg_returns404() throws Exception {
            when(certificateService.getCertificatesForUserInOrg(99L, 10L, 1L))
                    .thenThrow(new EntityNotFoundException("Organization not found"));

            mockMvc.perform(get("/certificates/user/99")
                            .with(authentication(authWithRole("OWNER"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Organization not found"));
        }
    }

    // =========================================================================
    // PATCH /certificates/{certId}
    // =========================================================================

    @Nested
    @DisplayName("PATCH /certificates/{certId}")
    class UpdateCertificateTests {

        @Test
        @DisplayName("OWNER can update — returns 200")
        void updateCertificate_owner_returns200() throws Exception {
            Certificate updated = new Certificate();
            updated.setId(200L);
            updated.setCertificateName("Updated Cert");
            updated.setUser(targetUser);
            updated.setFile(testFile);
            updated.setOrganization(testOrg);
            updated.setCourse(testCourse);

            UpdateCertificateRequest request = new UpdateCertificateRequest("Updated Cert", null, null);
            when(certificateService.updateCertificate(eq(200L), any(UpdateCertificateRequest.class), eq(10L), eq(1L)))
                    .thenReturn(updated);

            mockMvc.perform(patch("/certificates/200")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certificateName").value("Updated Cert"));
        }

        @Test
        @DisplayName("WORKER cannot update — returns 403")
        void updateCertificate_worker_returns403() throws Exception {
            UpdateCertificateRequest request = new UpdateCertificateRequest("Name", null, null);

            mockMvc.perform(patch("/certificates/200")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(certificateService, never()).updateCertificate(any(), any(), any(), any());
        }

        @Test
        @DisplayName("returns 404 when certificate not found")
        void updateCertificate_notFound_returns404() throws Exception {
            UpdateCertificateRequest request = new UpdateCertificateRequest("Name", null, null);
            when(certificateService.updateCertificate(eq(999L), any(), eq(10L), eq(1L)))
                    .thenThrow(new EntityNotFoundException("Certificate not found"));

            mockMvc.perform(patch("/certificates/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Certificate not found"));
        }
    }

    // =========================================================================
    // DELETE /certificates/{certId}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /certificates/{certId}")
    class DeleteCertificateTests {

        @Test
        @DisplayName("OWNER can delete — returns 204")
        void deleteCertificate_owner_returns204() throws Exception {
            mockMvc.perform(delete("/certificates/200")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(certificateService).deleteCertificate(200L, 10L, 1L);
        }

        @Test
        @DisplayName("MANAGER can delete — returns 204")
        void deleteCertificate_manager_returns204() throws Exception {
            mockMvc.perform(delete("/certificates/200")
                            .with(authentication(authWithRole("MANAGER")))
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(certificateService).deleteCertificate(200L, 10L, 1L);
        }

        @Test
        @DisplayName("WORKER cannot delete — returns 403")
        void deleteCertificate_worker_returns403() throws Exception {
            mockMvc.perform(delete("/certificates/200")
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(certificateService, never()).deleteCertificate(any(), any(), any());
        }

        @Test
        @DisplayName("returns 404 when certificate not found")
        void deleteCertificate_notFound_returns404() throws Exception {
            doThrow(new EntityNotFoundException("Certificate not found"))
                    .when(certificateService).deleteCertificate(999L, 10L, 1L);

            mockMvc.perform(delete("/certificates/999")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Certificate not found"));
        }
    }
}
