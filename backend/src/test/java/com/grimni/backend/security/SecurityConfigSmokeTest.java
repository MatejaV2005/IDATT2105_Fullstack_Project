package com.grimni.backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.grimni.controller.AuthController;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.security.SecurityConfig;
import com.grimni.service.OrganizationService;
import com.grimni.service.RefreshTokenService;
import com.grimni.service.UserService;
import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

/**
 * Smoke test verifying SecurityConfig's request authorization rules:
 * public endpoints are reachable without auth, everything else is denied.
 *
 * AuthController is loaded purely as a vehicle so MockMvc has at least one
 * mapped controller; we don't exercise its handlers here. JwtAuthFilter is
 * mocked to a no-op so it forwards every request without touching JwtUtil.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class SecurityConfigSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    // Filter is mocked: with no stubbing it does nothing in doFilter, but Spring
    // wraps it via OncePerRequestFilter — to keep things simple we mock it as a
    // bean and let Spring's filter chain proceed (the mock's doFilter is a no-op,
    // which still allows the chain to continue because addFilterBefore wraps it).
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private OrgUserBridgeRepository orgUserBridgeRepository;

    @MockitoBean
    private OrganizationService organizationService;

    @BeforeEach
    void stubFilterPassthrough() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest req = invocation.getArgument(0);
            HttpServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
    }

    @Test
    @DisplayName("/auth/login is publicly reachable (not 401/403)")
    void authLogin_isPublic() throws Exception {
        mockMvc.perform(post("/auth/login"))
                .andExpect(status().is(not401or403()));
    }

    @Test
    @DisplayName("/auth/register is publicly reachable")
    void authRegister_isPublic() throws Exception {
        mockMvc.perform(post("/auth/register"))
                .andExpect(status().is(not401or403()));
    }

    @Test
    @DisplayName("/auth/refresh is publicly reachable")
    void authRefresh_isPublic() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().is(not401or403()));
    }

    @Test
    @DisplayName("/health is publicly reachable")
    void health_isPublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().is(not401or403()));
    }

    @Test
    @DisplayName("/swagger-ui/index.html is publicly reachable")
    void swaggerUi_isPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().is(not401or403()));
    }

    @Test
    @DisplayName("/v3/api-docs is publicly reachable")
    void apiDocs_isPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is(not401or403()));
    }

    @Test
    @DisplayName("Unauthenticated request to a non-public path is rejected")
    void protectedPath_unauthenticated_isForbidden() throws Exception {
        mockMvc.perform(get("/me"))
                .andExpect(status().is(is401or403()));
    }

    @Test
    @DisplayName("Unauthenticated request to an unknown protected path is rejected")
    void unknownProtectedPath_unauthenticated_isForbidden() throws Exception {
        mockMvc.perform(get("/api/something/private"))
                .andExpect(status().is(is401or403()));
    }

    private static org.hamcrest.Matcher<Integer> not401or403() {
        return new org.hamcrest.BaseMatcher<Integer>() {
            @Override
            public boolean matches(Object actual) {
                int code = (Integer) actual;
                return code != 401 && code != 403;
            }
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("status not equal to 401 or 403");
            }
        };
    }

    private static org.hamcrest.Matcher<Integer> is401or403() {
        return new org.hamcrest.BaseMatcher<Integer>() {
            @Override
            public boolean matches(Object actual) {
                int code = (Integer) actual;
                return code == 401 || code == 403;
            }
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("status 401 or 403");
            }
        };
    }
}
