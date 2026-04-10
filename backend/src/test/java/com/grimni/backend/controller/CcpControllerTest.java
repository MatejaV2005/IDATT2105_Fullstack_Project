package com.grimni.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimni.controller.CcpController;
import com.grimni.dto.CcpCorrectiveMeasureResponse;
import com.grimni.dto.CcpHistoryResponse;
import com.grimni.dto.CcpIntervalResponse;
import com.grimni.dto.CcpResponse;
import com.grimni.dto.CcpUserResponse;
import com.grimni.dto.CollaboratorResponse;
import com.grimni.dto.CreateCcpCorrectiveMeasureRequest;
import com.grimni.dto.CreateCcpRequest;
import com.grimni.dto.ReplaceCcpAssignmentsRequest;
import com.grimni.dto.UpdateCcpCorrectiveMeasureRequest;
import com.grimni.dto.UpdateCcpFullRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.security.SecurityConfig;
import com.grimni.service.CcpService;
import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(CcpController.class)
@Import(SecurityConfig.class)
public class CcpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CcpService ccpService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Nested
    @DisplayName("GET /haccp/critical-control-points/get-all-info")
    class GetAllInfoTests {

        @Test
        @DisplayName("returns aggregate CCP info for the active organization")
        void getAllInfo_success() throws Exception {
            when(ccpService.getAllInfo(1L, 10L)).thenReturn(List.of(
                new CcpResponse(
                    33L,
                    "Varmholding ved servering",
                    "Måles hver 30. minutt under service.",
                    "Digitalt stikktermometer.",
                    "Termometer desinfiseres mellom hver måling.",
                    "Øk varme umiddelbart.",
                    new BigDecimal("60.0"),
                    new BigDecimal("90.0"),
                    "C",
                    "Supper og sauser.",
                    new CcpIntervalResponse(99L, 1764950400L, 1800L),
                    "Starts 2025-12-05 17:00, repeats every 30 minutes",
                    List.of(new CcpUserResponse(1L, "Kari Næss Northun")),
                    List.of(new CcpUserResponse(2L, "Simen Velle")),
                    List.of(new CcpUserResponse(3L, "Jens Stoltenberg")),
                    List.of(new CcpUserResponse(4L, "Gro Harlem Brundtland")),
                    List.of(new CcpCorrectiveMeasureResponse(501L, 90L, "Suppe", "Fortsett oppvarming"))
                )
            ));

            mockMvc.perform(get("/haccp/critical-control-points/get-all-info")
                    .with(authentication(authWithRole("WORKER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(33))
                .andExpect(jsonPath("$[0].name").value("Varmholding ved servering"))
                .andExpect(jsonPath("$[0].deviationReceivers[0].legalName").value("Simen Velle"))
                .andExpect(jsonPath("$[0].ccpCorrectiveMeasures[0].productName").value("Suppe"));
        }
    }

    @Nested
    @DisplayName("POST /haccp/critical-control-points")
    class CreateCcpTests {

        @Test
        @DisplayName("returns HTTP 201 with the created CCP")
        void createCcp_success() throws Exception {
            CreateCcpRequest request = new CreateCcpRequest(
                "Varmholding ved servering",
                "Måles hver 30. minutt under service.",
                "Digitalt stikktermometer.",
                "Termometer desinfiseres mellom hver måling.",
                "Øk varme umiddelbart.",
                new BigDecimal("60.0"),
                new BigDecimal("90.0"),
                "C",
                "Supper og sauser.",
                1764950400L,
                1800L,
                List.of(1L),
                List.of(2L),
                List.of(3L),
                List.of(4L),
                List.of(new CreateCcpCorrectiveMeasureRequest(90L, "Fortsett oppvarming"))
            );

            when(ccpService.createCcp(any(), eq(1L), eq(10L))).thenReturn(
                new CcpResponse(
                    33L,
                    "Varmholding ved servering",
                    "Måles hver 30. minutt under service.",
                    "Digitalt stikktermometer.",
                    "Termometer desinfiseres mellom hver måling.",
                    "Øk varme umiddelbart.",
                    new BigDecimal("60.0"),
                    new BigDecimal("90.0"),
                    "C",
                    "Supper og sauser.",
                    new CcpIntervalResponse(99L, 1764950400L, 1800L),
                    "Starts 2025-12-05 17:00, repeats every 30 minutes",
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
                )
            );

            mockMvc.perform(post("/haccp/critical-control-points")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(33))
                .andExpect(jsonPath("$.name").value("Varmholding ved servering"));
        }
    }

    @Nested
    @DisplayName("PUT /haccp/critical-control-points")
    class UpdateCcpFullTests {

        @Test
        @DisplayName("MANAGER can fully update a CCP — returns 200 with refreshed resource")
        void updateCcpFull_success() throws Exception {
            UpdateCcpFullRequest request = new UpdateCcpFullRequest(
                33L,
                "Updated name",
                "Updated how",
                "Updated equipment",
                "Updated instructions",
                "Updated immediate action",
                new BigDecimal("65.0"),
                new BigDecimal("85.0"),
                "C",
                "Updated description",
                List.of(1L),
                List.of(),
                List.of(2L),
                List.of(),
                List.of(new CreateCcpCorrectiveMeasureRequest(90L, "Re-heat to 75C"))
            );

            when(ccpService.updateCcpFull(any(UpdateCcpFullRequest.class), eq(1L), eq(10L))).thenReturn(
                new CcpResponse(
                    33L,
                    "Updated name",
                    "Updated how",
                    "Updated equipment",
                    "Updated instructions",
                    "Updated immediate action",
                    new BigDecimal("65.0"),
                    new BigDecimal("85.0"),
                    "C",
                    "Updated description",
                    new CcpIntervalResponse(99L, 1764950400L, 1800L),
                    "Starts 2025-12-05 17:00, repeats every 30 minutes",
                    List.of(new CcpUserResponse(1L, "Kari Næss Northun")),
                    List.of(),
                    List.of(new CcpUserResponse(2L, "Jens Stoltenberg")),
                    List.of(),
                    List.of(new CcpCorrectiveMeasureResponse(777L, 90L, "Burger", "Re-heat to 75C"))
                )
            );

            mockMvc.perform(put("/haccp/critical-control-points")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(33))
                .andExpect(jsonPath("$.name").value("Updated name"))
                .andExpect(jsonPath("$.verifiers[0].userId").value(1))
                .andExpect(jsonPath("$.performers[0].legalName").value("Jens Stoltenberg"))
                .andExpect(jsonPath("$.ccpCorrectiveMeasures[0].measureDescription").value("Re-heat to 75C"));
        }

        @Test
        @DisplayName("returns HTTP 400 when service rejects threshold ordering")
        void updateCcpFull_invalidThresholds_returnsBadRequest() throws Exception {
            UpdateCcpFullRequest request = new UpdateCcpFullRequest(
                33L, null, null, null, null, null,
                new BigDecimal("100.0"), new BigDecimal("50.0"),
                null, null, List.of(), List.of(), List.of(), List.of(), List.of()
            );

            when(ccpService.updateCcpFull(any(UpdateCcpFullRequest.class), eq(1L), eq(10L)))
                .thenThrow(new IllegalArgumentException("Critical minimum cannot be greater than critical maximum"));

            mockMvc.perform(put("/haccp/critical-control-points")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Critical minimum cannot be greater than critical maximum"));
        }

        @Test
        @DisplayName("WORKER cannot fully update a CCP — returns 403")
        void updateCcpFull_workerForbidden() throws Exception {
            UpdateCcpFullRequest request = new UpdateCcpFullRequest(
                33L, "name", "how", "eq", "inst", "imm",
                new BigDecimal("60.0"), new BigDecimal("90.0"),
                "C", "desc", List.of(), List.of(), List.of(), List.of(), List.of()
            );

            mockMvc.perform(put("/haccp/critical-control-points")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("WORKER")))
                    .with(csrf()))
                .andExpect(status().isForbidden());

            verify(ccpService, never()).updateCcpFull(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("PUT /haccp/critical-control-points/{ccpId}/assignments")
    class ReplaceAssignmentsTests {

        @Test
        @DisplayName("returns HTTP 400 when assigned users are outside the active organization")
        void replaceAssignments_invalidUsers_returnsBadRequest() throws Exception {
            ReplaceCcpAssignmentsRequest request = new ReplaceCcpAssignmentsRequest(
                List.of(1L),
                List.of(2L),
                List.of(3L),
                List.of(4L)
            );

            when(ccpService.replaceAssignments(any(), any(), eq(1L), eq(10L)))
                .thenThrow(new IllegalArgumentException("Assigned users must belong to the active organization"));

            mockMvc.perform(put("/haccp/critical-control-points/33/assignments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Assigned users must belong to the active organization"));
        }

        @Test
        @DisplayName("workers cannot replace CCP assignments")
        void replaceAssignments_workerForbidden() throws Exception {
            ReplaceCcpAssignmentsRequest request = new ReplaceCcpAssignmentsRequest(
                List.of(1L),
                List.of(),
                List.of(),
                List.of()
            );

            mockMvc.perform(put("/haccp/critical-control-points/33/assignments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("WORKER")))
                    .with(csrf()))
                .andExpect(status().isForbidden());

            verify(ccpService, never()).replaceAssignments(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("CCP corrective measure endpoints")
    class CorrectiveMeasureTests {

        @Test
        @DisplayName("creates a corrective measure")
        void createCorrectiveMeasure_success() throws Exception {
            CreateCcpCorrectiveMeasureRequest request =
                new CreateCcpCorrectiveMeasureRequest(90L, "Fortsett oppvarming");

            when(ccpService.createCorrectiveMeasure(any(), any(), eq(1L), eq(10L)))
                .thenReturn(new CcpCorrectiveMeasureResponse(501L, 90L, "Suppe", "Fortsett oppvarming"));

            mockMvc.perform(post("/haccp/critical-control-points/33/corrective-measures")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(501))
                .andExpect(jsonPath("$.productName").value("Suppe"));
        }

        @Test
        @DisplayName("updates a corrective measure")
        void updateCorrectiveMeasure_success() throws Exception {
            UpdateCcpCorrectiveMeasureRequest request =
                new UpdateCcpCorrectiveMeasureRequest(91L, "Skift beholder og dokumenter avvik.");

            when(ccpService.updateCorrectiveMeasure(any(), any(), eq(1L), eq(10L)))
                .thenReturn(new CcpCorrectiveMeasureResponse(501L, 91L, "Saus", "Skift beholder og dokumenter avvik."));

            mockMvc.perform(patch("/haccp/critical-control-points/corrective-measures/501")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Saus"));
        }

        @Test
        @DisplayName("deletes a corrective measure")
        void deleteCorrectiveMeasure_success() throws Exception {
            mockMvc.perform(delete("/haccp/critical-control-points/corrective-measures/501")
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("GET /ccps/verification-count")
    class GetVerificationCountTests {

        @Test
        @DisplayName("returns count for authenticated user")
        void getVerificationCount_success() throws Exception {
            when(ccpService.getVerificationCount(1L, 10L, "MANAGER")).thenReturn(7L);

            mockMvc.perform(get("/ccps/verification-count")
                    .with(authentication(authWithRole("MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
        }

        @Test
        @DisplayName("returns count for WORKER role")
        void getVerificationCount_worker() throws Exception {
            when(ccpService.getVerificationCount(1L, 10L, "WORKER")).thenReturn(2L);

            mockMvc.perform(get("/ccps/verification-count")
                    .with(authentication(authWithRole("WORKER"))))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
        }

        @Test
        @DisplayName("unauthenticated returns 403")
        void getVerificationCount_unauthenticated() throws Exception {
            mockMvc.perform(get("/ccps/verification-count"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /ccps/logs")
    class GetVerificationLogsTests {

        @Test
        @DisplayName("returns grouped CCP records")
        void getVerificationLogs_success() throws Exception {
            when(ccpService.getVerificationLogs(1L, 10L, "MANAGER")).thenReturn(List.of(
                new CcpHistoryResponse(33L, "Kjøleskap temperatur", List.of(
                    new CcpHistoryResponse.CcpRecordResponse(
                        11L,
                        new java.math.BigDecimal("2.4"),
                        new java.math.BigDecimal("2"),
                        new java.math.BigDecimal("4"),
                        "C",
                        "Kjøleskapet lager en rar lyd",
                        new CollaboratorResponse(5L, "Per Willy Amundsen")
                    )
                ))
            ));

            mockMvc.perform(get("/ccps/logs")
                    .with(authentication(authWithRole("MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(33))
                .andExpect(jsonPath("$[0].name").value("Kjøleskap temperatur"))
                .andExpect(jsonPath("$[0].records[0].id").value(11))
                .andExpect(jsonPath("$[0].records[0].value").value(2.4))
                .andExpect(jsonPath("$[0].records[0].min").value(2))
                .andExpect(jsonPath("$[0].records[0].max").value(4))
                .andExpect(jsonPath("$[0].records[0].unit").value("C"))
                .andExpect(jsonPath("$[0].records[0].performedBy.userId").value(5))
                .andExpect(jsonPath("$[0].records[0].performedBy.legalName").value("Per Willy Amundsen"));
        }

        @Test
        @DisplayName("returns empty list when no records")
        void getVerificationLogs_empty() throws Exception {
            when(ccpService.getVerificationLogs(1L, 10L, "WORKER")).thenReturn(List.of());

            mockMvc.perform(get("/ccps/logs")
                    .with(authentication(authWithRole("WORKER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("unauthenticated returns 403")
        void getVerificationLogs_unauthenticated() throws Exception {
            mockMvc.perform(get("/ccps/logs"))
                .andExpect(status().isForbidden());
        }
    }
}
