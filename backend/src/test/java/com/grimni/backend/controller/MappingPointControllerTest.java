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
import com.grimni.controller.MappingPointController;
import com.grimni.dto.CreateMappingPointRequest;
import com.grimni.dto.MappingPointResponse;
import com.grimni.dto.UpdateMappingPointRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.security.SecurityConfig;
import com.grimni.service.MappingPointService;
import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(MappingPointController.class)
@Import(SecurityConfig.class)
public class MappingPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MappingPointService mappingPointService;

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
    @DisplayName("GET /mapping-points")
    class GetAllInfoTests {

        @Test
        @DisplayName("returns mapping points for the active organization")
        void getAllInfo_success() throws Exception {
            when(mappingPointService.getAllInfo(1L, 10L)).thenReturn(List.of(
                new MappingPointResponse(
                    17L,
                    "AL § 1-5",
                    (short) 8,
                    "Salg eller utlevering til person som er under 18 år",
                    "Mindreårige kunder bruker lånt/falskt ID-kort.",
                    "Instruks om å sjekke legitimasjon.",
                    "Hvem enn som er i kassen ved gitt tidspunkt"
                )
            ));

            mockMvc.perform(get("/mapping-points")
                    .with(authentication(authWithRole("WORKER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(17))
                .andExpect(jsonPath("$[0].law").value("AL § 1-5"))
                .andExpect(jsonPath("$[0].dots").value(8))
                .andExpect(jsonPath("$[0].responsibleForPoint").value("Hvem enn som er i kassen ved gitt tidspunkt"));
        }
    }

    @Nested
    @DisplayName("POST /mapping-points")
    class CreateTests {

        @Test
        @DisplayName("returns HTTP 201 with the created mapping point")
        void create_success() throws Exception {
            CreateMappingPointRequest request = new CreateMappingPointRequest(
                "AL § 1-5",
                (short) 8,
                "Salg eller utlevering til person som er under 18 år",
                "Mindreårige kunder bruker lånt/falskt ID-kort.",
                "Instruks om å sjekke legitimasjon.",
                "Hvem enn som er i kassen ved gitt tidspunkt"
            );

            when(mappingPointService.createMappingPoint(any(), eq(1L), eq(10L))).thenReturn(
                new MappingPointResponse(
                    17L,
                    request.law(),
                    request.dots(),
                    request.title(),
                    request.challenges(),
                    request.measures(),
                    request.responsibleForPoint()
                )
            );

            mockMvc.perform(post("/mapping-points")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.responsibleForPoint").value("Hvem enn som er i kassen ved gitt tidspunkt"));
        }

        @Test
        @DisplayName("workers cannot create mapping points")
        void create_workerForbidden() throws Exception {
            CreateMappingPointRequest request = new CreateMappingPointRequest(
                "AL § 1-5",
                (short) 8,
                "Tittel",
                "Utfordringer",
                "Tiltak",
                "Kasseansvarlig"
            );

            mockMvc.perform(post("/mapping-points")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("WORKER")))
                    .with(csrf()))
                .andExpect(status().isForbidden());

            verify(mappingPointService, never()).createMappingPoint(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("PATCH /mapping-points/{mappingPointId}")
    class UpdateTests {

        @Test
        @DisplayName("updates mapping point fields")
        void update_success() throws Exception {
            UpdateMappingPointRequest request = new UpdateMappingPointRequest(
                null,
                null,
                null,
                null,
                "Oppdatert tiltak",
                "Ny ansvarstekst"
            );

            when(mappingPointService.updateMappingPoint(eq(17L), any(), eq(1L), eq(10L))).thenReturn(
                new MappingPointResponse(
                    17L,
                    "AL § 1-5",
                    (short) 8,
                    "Salg eller utlevering til person som er under 18 år",
                    "Mindreårige kunder bruker lånt/falskt ID-kort.",
                    "Oppdatert tiltak",
                    "Ny ansvarstekst"
                )
            );

            mockMvc.perform(patch("/mapping-points/17")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(authentication(authWithRole("MANAGER")))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measures").value("Oppdatert tiltak"))
                .andExpect(jsonPath("$.responsibleForPoint").value("Ny ansvarstekst"));
        }
    }

    @Nested
    @DisplayName("DELETE /mapping-points/{mappingPointId}")
    class DeleteTests {

        @Test
        @DisplayName("deletes a mapping point")
        void delete_success() throws Exception {
            mockMvc.perform(delete("/mapping-points/17")
                    .with(authentication(authWithRole("OWNER")))
                    .with(csrf()))
                .andExpect(status().isNoContent());

            verify(mappingPointService).deleteMappingPoint(17L, 1L, 10L);
        }
    }
}
