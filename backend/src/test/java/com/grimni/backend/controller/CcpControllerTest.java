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
import com.grimni.dto.CcpIntervalResponse;
import com.grimni.dto.CcpResponse;
import com.grimni.dto.CcpUserResponse;
import com.grimni.dto.CreateCcpCorrectiveMeasureRequest;
import com.grimni.dto.CreateCcpRequest;
import com.grimni.dto.ReplaceCcpAssignmentsRequest;
import com.grimni.dto.UpdateCcpCorrectiveMeasureRequest;
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
}
