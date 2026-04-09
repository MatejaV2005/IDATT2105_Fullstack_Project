package com.grimni.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimni.controller.PrerequisiteCategoryController;
import com.grimni.dto.PrerequisiteCategoryAllInfoResponse;
import com.grimni.dto.PrerequisiteIntervalResponse;
import com.grimni.dto.PrerequisiteUserResponse;
import com.grimni.dto.ReplaceRoutineAssignmentsRequest;
import com.grimni.dto.RoutinePrerequisitePointResponse;
import com.grimni.dto.StandardPrerequisitePointResponse;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.security.SecurityConfig;
import com.grimni.service.PrerequisiteCategoryService;
import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(PrerequisiteCategoryController.class)
@Import(SecurityConfig.class)
public class PrerequisiteCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrerequisiteCategoryService prerequisiteCategoryService;

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
    @DisplayName("GET /prerequisite-categories/get-all-info")
    class GetAllInfoTests {

        @Test
        @DisplayName("returns aggregate prerequisite info for the active organization")
        void getAllInfo_success() throws Exception {
            RoutinePrerequisitePointResponse routinePoint = new RoutinePrerequisitePointResponse(
                17L,
                "Vask gulvene",
                "routine",
                "Vask gulvene etter stengetid.",
                "Vask ekstra godt neste gang",
                "Starts 2025-12-05 17:00, repeats every 1 week",
                new PrerequisiteIntervalResponse(9L, 1764950400L, 604800L),
                List.of(new PrerequisiteUserResponse(1L, "Kari Næss Northun")),
                List.of(new PrerequisiteUserResponse(2L, "Simen Velle")),
                List.of(new PrerequisiteUserResponse(3L, "Jonas Gahr Støre")),
                List.of(new PrerequisiteUserResponse(4L, "Gro Harlem Brundtland"))
            );

            StandardPrerequisitePointResponse standardPoint = new StandardPrerequisitePointResponse(
                18L,
                "Hold rent",
                "standard",
                "Vask 1 skal kun brukes for mat"
            );

            when(prerequisiteCategoryService.getAllInfo(1L, 10L)).thenReturn(List.of(
                new PrerequisiteCategoryAllInfoResponse(
                    11L,
                    "Renhold av lokaler og utstyr",
                    List.of(routinePoint, standardPoint)
                )
            ));

            mockMvc.perform(get("/prerequisite-categories/get-all-info")
                    .with(authentication(authWithRole("WORKER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].categoryName").value("Renhold av lokaler og utstyr"))
                .andExpect(jsonPath("$[0].points[0].type").value("routine"))
                .andExpect(jsonPath("$[0].points[0].verifiers[0].legalName").value("Kari Næss Northun"))
                .andExpect(jsonPath("$[0].points[1].type").value("standard"));
        }
    }

    @Nested
    @DisplayName("POST /prerequisite-categories/{categoryId}/routines")
    class CreateRoutineTests {

        @Test
        @DisplayName("returns HTTP 201 with the created routine")
        void createRoutine_success() throws Exception {
            String requestBody = """
                {
                  "title": "Vask gulvene",
                  "description": "Vask gulvene etter stengetid.",
                  "measures": "Vask ekstra godt neste gang",
                  "intervalStart": 1764950400,
                  "intervalRepeatTime": 604800,
                  "verifierUserIds": [1],
                  "deviationReceiverUserIds": [2],
                  "performerUserIds": [3],
                  "deputyUserIds": [4]
                }
                """;

            when(prerequisiteCategoryService.createRoutine(any(), any(), eq(1L), eq(10L))).thenReturn(
                new RoutinePrerequisitePointResponse(
                    17L,
                    "Vask gulvene",
                    "routine",
                    "Vask gulvene etter stengetid.",
                    "Vask ekstra godt neste gang",
                    "Starts 2025-12-05 17:00, repeats every 1 week",
                    new PrerequisiteIntervalResponse(9L, 1764950400L, 604800L),
                    List.of(new PrerequisiteUserResponse(1L, "Kari Næss Northun")),
                    List.of(),
                    List.of(),
                    List.of()
                )
            );

            mockMvc.perform(post("/prerequisite-categories/12/routines")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.description").value("Vask gulvene etter stengetid."))
                .andExpect(jsonPath("$.type").value("routine"));
        }
    }

    @Nested
    @DisplayName("PUT /prerequisite-routines/{routineId}/assignments")
    class ReplaceAssignmentsTests {

        @Test
        @DisplayName("returns HTTP 400 when assigned users are outside the active organization")
        void replaceAssignments_invalidUsers_returnsBadRequest() throws Exception {
            ReplaceRoutineAssignmentsRequest request = new ReplaceRoutineAssignmentsRequest(
                List.of(1L, 2L),
                List.of(3L),
                List.of(4L),
                List.of(5L)
            );

            when(prerequisiteCategoryService.replaceRoutineAssignments(any(), any(), eq(1L), eq(10L)))
                .thenThrow(new IllegalArgumentException("Assigned users must belong to the active organization"));

            mockMvc.perform(put("/prerequisite-routines/17/assignments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Assigned users must belong to the active organization"));
        }

        @Test
        @DisplayName("workers cannot replace assignments")
        void replaceAssignments_workerForbidden() throws Exception {
            ReplaceRoutineAssignmentsRequest request = new ReplaceRoutineAssignmentsRequest(
                List.of(1L),
                List.of(),
                List.of(),
                List.of()
            );

            mockMvc.perform(put("/prerequisite-routines/17/assignments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("WORKER")))
                    .with(csrf()))
                .andExpect(status().isForbidden());

            verify(prerequisiteCategoryService, never()).replaceRoutineAssignments(any(), any(), any(), any());
        }
    }
}
