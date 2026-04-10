package com.grimni.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimni.controller.AuthController;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.RefreshToken;
import com.grimni.domain.User;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.dto.LoginRequest;
import com.grimni.dto.RegisterRequest;
import com.grimni.service.OrganizationService;
import com.grimni.service.RefreshTokenService;
import com.grimni.service.UserService;
import com.grimni.util.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import org.springframework.security.authentication.BadCredentialsException;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RefreshTokenService refService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private OrganizationService organizationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private OrgUserBridge testBridge;
    private ResponseCookie testCookie;
    private RefreshToken storedToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        testUser.setLegalName("Alice");
        testUser.setEmail("alice@example.com");

        Organization testOrg = new Organization();
        testOrg.setId(10L);
        testBridge = new OrgUserBridge();
        testBridge.setOrganization(testOrg);
        testBridge.setUser(testUser);
        testBridge.setUserRole(OrgUserRole.MANAGER);

        testCookie = ResponseCookie.from("refresh_token", "cookie-plaintext")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        storedToken = new RefreshToken();
        storedToken.setUser(testUser);
        storedToken.setOrganization(testOrg);
    }

    private static UsernamePasswordAuthenticationToken authWithRole(String role) {
        return new UsernamePasswordAuthenticationToken(
                new com.grimni.security.JwtUserPrinciple(1L, 10L, "Alice", role),
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
    }

    // -------------------------------------------------------------------------
    // POST /auth/register
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/register — success")
    class RegisterSuccessTests {

        @Test
        @DisplayName("returns HTTP 201 when valid registration data is provided")
        void register_validData_returns201() throws Exception {
            when(userService.register(any(User.class))).thenReturn(testUser);

            RegisterRequest request = new RegisterRequest("alice@example.com", "password123", "Alice");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("response body contains success message")
        void register_validData_returnsSuccessMessage() throws Exception {
            when(userService.register(any(User.class))).thenReturn(testUser);

            RegisterRequest request = new RegisterRequest("alice@example.com", "password123", "Alice");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().string("User registered successfully"));
        }

        @Test
        @DisplayName("userService.register() is called with a user object")
        void register_verifiesUserServiceCalled() throws Exception {
            when(userService.register(any(User.class))).thenReturn(testUser);

            RegisterRequest request = new RegisterRequest("alice@example.com", "password123", "Alice");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(userService).register(any(User.class));
        }
    }

    @Nested
    @DisplayName("POST /auth/register — failure")
    class RegisterFailureTests {

        @Test
        @DisplayName("returns HTTP 400 when email is blank")
        void register_blankEmail_returns400() throws Exception {
            RegisterRequest request = new RegisterRequest("", "password123", "Alice");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns HTTP 400 when password is blank")
        void register_blankPassword_returns400() throws Exception {
            RegisterRequest request = new RegisterRequest("alice@example.com", "", "Alice");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns HTTP 400 when legalName is blank")
        void register_blankLegalName_returns400() throws Exception {
            RegisterRequest request = new RegisterRequest("alice@example.com", "password123", "");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns HTTP 400 when email already exists")
        void register_duplicateEmail_returns400() throws Exception {
            when(userService.register(any(User.class)))
                    .thenThrow(new IllegalArgumentException("Email already exists"));

            RegisterRequest request = new RegisterRequest("alice@example.com", "password123", "Alice");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // POST /auth/login — success (user with org)
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/login — success with org")
    class LoginSuccessWithOrgTests {

        @Test
        @DisplayName("returns HTTP 200 when valid credentials are provided")
        void login_validCredentials_returns200() throws Exception {
            when(userService.login("alice@example.com", "correctpass")).thenReturn(testUser);
            when(organizationService.findDefaultMembershipForUser(1L)).thenReturn(Optional.of(testBridge));
            when(jwtUtil.generateToken(eq(testUser), eq(testBridge))).thenReturn("jwt-token-abc");
            when(refService.createAndStoreRefreshToken(eq(testUser), eq(testBridge.getOrganization()))).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice@example.com", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("response body contains the JWT string")
        void login_responseBodyContainsJwt() throws Exception {
            when(userService.login("alice@example.com", "correctpass")).thenReturn(testUser);
            when(organizationService.findDefaultMembershipForUser(1L)).thenReturn(Optional.of(testBridge));
            when(jwtUtil.generateToken(eq(testUser), eq(testBridge))).thenReturn("jwt-token-abc");
            when(refService.createAndStoreRefreshToken(eq(testUser), eq(testBridge.getOrganization()))).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice@example.com", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().string("jwt-token-abc"));
        }

        @Test
        @DisplayName("response headers contain Set-Cookie with the refresh token")
        void login_responseContainsSetCookieHeader() throws Exception {
            when(userService.login("alice@example.com", "correctpass")).thenReturn(testUser);
            when(organizationService.findDefaultMembershipForUser(1L)).thenReturn(Optional.of(testBridge));
            when(jwtUtil.generateToken(eq(testUser), eq(testBridge))).thenReturn("jwt-token-abc");
            when(refService.createAndStoreRefreshToken(eq(testUser), eq(testBridge.getOrganization()))).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice@example.com", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().string("Set-Cookie", containsString("refresh_token")));
        }
    }

    // -------------------------------------------------------------------------
    // POST /auth/login — success (user without org)
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/login — success without org")
    class LoginSuccessWithoutOrgTests {

        @Test
        @DisplayName("returns HTTP 200 when user has no organization")
        void login_noOrg_returns200() throws Exception {
            when(userService.login("alice@example.com", "correctpass")).thenReturn(testUser);
            when(organizationService.findDefaultMembershipForUser(1L)).thenReturn(Optional.empty());
            when(jwtUtil.generateToken(eq(testUser), eq(null))).thenReturn("jwt-no-org");
            when(refService.createAndStoreRefreshToken(eq(testUser), eq(null))).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice@example.com", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("JWT is generated with null bridge when user has no org")
        void login_noOrg_generatesTokenWithNullBridge() throws Exception {
            when(userService.login("alice@example.com", "correctpass")).thenReturn(testUser);
            when(organizationService.findDefaultMembershipForUser(1L)).thenReturn(Optional.empty());
            when(jwtUtil.generateToken(eq(testUser), eq(null))).thenReturn("jwt-no-org");
            when(refService.createAndStoreRefreshToken(eq(testUser), eq(null))).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice@example.com", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(jwtUtil).generateToken(eq(testUser), eq(null));
        }

        @Test
        @DisplayName("response body contains JWT even without org context")
        void login_noOrg_responseContainsJwt() throws Exception {
            when(userService.login("alice@example.com", "correctpass")).thenReturn(testUser);
            when(organizationService.findDefaultMembershipForUser(1L)).thenReturn(Optional.empty());
            when(jwtUtil.generateToken(eq(testUser), eq(null))).thenReturn("jwt-no-org");
            when(refService.createAndStoreRefreshToken(eq(testUser), eq(null))).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice@example.com", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().string("jwt-no-org"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /auth/login — failure
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/login — failure")
    class LoginFailureTests {

        @Test
        @DisplayName("returns HTTP 401 when user is not found")
        void login_userNotFound_returns401() throws Exception {
            when(userService.login("ghost@example.com", "pass"))
                    .thenThrow(new BadCredentialsException("Invalid email or password"));

            LoginRequest request = new LoginRequest("ghost@example.com", "pass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("returns HTTP 401 when password is incorrect")
        void login_invalidPassword_returns401() throws Exception {
            when(userService.login("alice@example.com", "wrongpass"))
                    .thenThrow(new BadCredentialsException("Invalid email or password"));

            LoginRequest request = new LoginRequest("alice@example.com", "wrongpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("refService is never called when login fails")
        void login_failure_refServiceNeverCalled() throws Exception {
            when(userService.login("alice@example.com", "wrongpass"))
                    .thenThrow(new BadCredentialsException("Invalid email or password"));

            LoginRequest request = new LoginRequest("alice@example.com", "wrongpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(refService, never()).createAndStoreRefreshToken(any(), any());
        }

        @Test
        @DisplayName("jwtUtil is never called when login fails")
        void login_failure_jwtUtilNeverCalled() throws Exception {
            when(userService.login("alice@example.com", "wrongpass"))
                    .thenThrow(new BadCredentialsException("Invalid email or password"));

            LoginRequest request = new LoginRequest("alice@example.com", "wrongpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil, never()).generateToken(any(), any());
        }
    }

    // -------------------------------------------------------------------------
    // POST /auth/refresh — success
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/refresh — success")
    class RefreshSuccessTests {

        @Test
        @DisplayName("returns HTTP 200 when a valid refresh_token cookie is present")
        void refresh_validCookie_returns200() throws Exception {
            when(refService.getStoredRefreshToken("old-token")).thenReturn(storedToken);
            when(organizationService.resolveRefreshMembership(1L, 10L)).thenReturn(testBridge);
            when(refService.rotateRefreshToken(storedToken, testBridge.getOrganization())).thenReturn(testCookie);
            when(jwtUtil.generateToken(eq(testUser), eq(testBridge))).thenReturn("new-jwt");

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "old-token")))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("response body contains the new JWT string")
        void refresh_responseBodyContainsNewJwt() throws Exception {
            when(refService.getStoredRefreshToken("old-token")).thenReturn(storedToken);
            when(organizationService.resolveRefreshMembership(1L, 10L)).thenReturn(testBridge);
            when(refService.rotateRefreshToken(storedToken, testBridge.getOrganization())).thenReturn(testCookie);
            when(jwtUtil.generateToken(eq(testUser), eq(testBridge))).thenReturn("new-jwt");

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "old-token")))
                    .andExpect(content().string("new-jwt"));
        }

        @Test
        @DisplayName("response headers contain a rotated Set-Cookie header")
        void refresh_responseContainsRotatedCookie() throws Exception {
            when(refService.getStoredRefreshToken("old-token")).thenReturn(storedToken);
            when(organizationService.resolveRefreshMembership(1L, 10L)).thenReturn(testBridge);
            when(refService.rotateRefreshToken(storedToken, testBridge.getOrganization())).thenReturn(testCookie);
            when(jwtUtil.generateToken(eq(testUser), eq(testBridge))).thenReturn("new-jwt");

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "old-token")))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().string("Set-Cookie", containsString("refresh_token")));
        }

        @Test
        @DisplayName("refresh falls back to the resolved active membership")
        void refresh_resolvesMembership_returns200() throws Exception {
            storedToken.setOrganization(null);
            when(refService.getStoredRefreshToken("old-token")).thenReturn(storedToken);
            when(organizationService.resolveRefreshMembership(1L, null)).thenReturn(testBridge);
            when(refService.rotateRefreshToken(storedToken, testBridge.getOrganization())).thenReturn(testCookie);
            when(jwtUtil.generateToken(eq(testUser), eq(testBridge))).thenReturn("jwt-with-org");

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "old-token")))
                    .andExpect(status().isOk())
                    .andExpect(content().string("jwt-with-org"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /auth/refresh — failure
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/refresh — failure")
    class RefreshFailureTests {

        @Test
        @DisplayName("returns HTTP 401 when refresh token is invalid")
        void refresh_invalidToken_returns401() throws Exception {
            when(refService.getStoredRefreshToken("bad-token"))
                    .thenThrow(new BadCredentialsException("Invalid refresh token"));

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "bad-token")))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("returns HTTP 400 when no refresh_token cookie is present")
        void refresh_noCookie_returns400() throws Exception {
            mockMvc.perform(post("/auth/refresh"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("rotateRefreshToken is never called when token lookup fails")
        void refresh_invalidToken_rotateNeverCalled() throws Exception {
            when(refService.getStoredRefreshToken("bad-token"))
                    .thenThrow(new BadCredentialsException("Invalid refresh token"));

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "bad-token")))
                    .andExpect(status().isUnauthorized());

            verify(refService, never()).rotateRefreshToken(any(RefreshToken.class), any());
        }

        @Test
        @DisplayName("jwtUtil is never called when token lookup fails")
        void refresh_invalidToken_jwtUtilNeverCalled() throws Exception {
            when(refService.getStoredRefreshToken("bad-token"))
                    .thenThrow(new BadCredentialsException("Invalid refresh token"));

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "bad-token")))
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil, never()).generateToken(any(), any());
        }
    }

    @Nested
    @DisplayName("POST /auth/switch-organization")
    class SwitchOrganizationTests {

        @Test
        @DisplayName("returns HTTP 200 with a new JWT when the target org is accessible")
        void switchOrganization_success() throws Exception {
            Organization nextOrg = new Organization();
            nextOrg.setId(25L);
            testBridge.setOrganization(nextOrg);

            when(refService.getStoredRefreshTokenForUser("old-token", 1L)).thenReturn(storedToken);
            when(organizationService.getMembershipForUser(1L, 25L)).thenReturn(testBridge);
            when(jwtUtil.generateToken(eq(testUser), eq(testBridge))).thenReturn("switched-jwt");
            when(refService.rotateRefreshToken(storedToken, nextOrg)).thenReturn(testCookie);

            mockMvc.perform(post("/auth/switch-organization")
                            .principal(authWithRole("WORKER"))
                            .with(authentication(authWithRole("WORKER")))
                            .cookie(new Cookie("refresh_token", "old-token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "organizationId": 25
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(content().string("switched-jwt"))
                    .andExpect(header().string("Set-Cookie", containsString("refresh_token")));
        }

        @Test
        @DisplayName("returns HTTP 401 when the refresh cookie belongs to another user")
        void switchOrganization_wrongRefreshToken_returns401() throws Exception {
            when(refService.getStoredRefreshTokenForUser("old-token", 1L))
                    .thenThrow(new BadCredentialsException("Refresh token does not belong to the authenticated user"));

            mockMvc.perform(post("/auth/switch-organization")
                            .principal(authWithRole("WORKER"))
                            .with(authentication(authWithRole("WORKER")))
                            .cookie(new Cookie("refresh_token", "old-token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "organizationId": 25
                                }
                                """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Refresh token does not belong to the authenticated user"));

            verify(organizationService, never()).getMembershipForUser(anyLong(), anyLong());
        }
    }

    // -------------------------------------------------------------------------
    // POST /auth/logout
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("returns HTTP 200 when a valid refresh_token cookie is present")
        void logout_validCookie_returns200() throws Exception {
            when(refService.getUserByRefreshToken("good-token")).thenReturn(testUser);

            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "good-token")))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns HTTP 200 even when no refresh_token cookie is present")
        void logout_noCookie_returns200() throws Exception {
            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns HTTP 200 even when the refresh_token cookie is invalid")
        void logout_invalidCookie_returns200() throws Exception {
            when(refService.getUserByRefreshToken("bad-token"))
                    .thenThrow(new BadCredentialsException("Invalid refresh token"));

            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "bad-token")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("response headers contain an expired Set-Cookie clearing refresh_token")
        void logout_responseContainsExpiredCookie() throws Exception {
            when(refService.getUserByRefreshToken("good-token")).thenReturn(testUser);

            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "good-token")))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().string("Set-Cookie", containsString("refresh_token")))
                    .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
        }

        @Test
        @DisplayName("userService.logout() is called with the resolved user")
        void logout_verifiesUserServiceLogoutCalled() throws Exception {
            when(refService.getUserByRefreshToken("good-token")).thenReturn(testUser);

            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "good-token")))
                    .andExpect(status().isOk());

            verify(userService).logout(testUser);
        }

        @Test
        @DisplayName("userService.logout() is never called when no cookie is present")
        void logout_noCookie_userServiceNeverCalled() throws Exception {
            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isOk());

            verify(userService, never()).logout(any());
        }

        @Test
        @DisplayName("userService.logout() is never called when cookie lookup fails")
        void logout_invalidCookie_userServiceNeverCalled() throws Exception {
            when(refService.getUserByRefreshToken("bad-token"))
                    .thenThrow(new BadCredentialsException("Invalid refresh token"));

            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "bad-token")))
                    .andExpect(status().isOk());

            verify(userService, never()).logout(any());
        }
    }
}
