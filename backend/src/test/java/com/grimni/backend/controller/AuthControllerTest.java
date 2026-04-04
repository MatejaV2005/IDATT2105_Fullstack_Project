package com.grimni.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimni.controller.AuthController;
import com.grimni.domain.User;
import com.grimni.dto.LoginRequest;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import java.time.Duration;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private ResponseCookie testCookie;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("alice");

        testCookie = ResponseCookie.from("refresh_token", "cookie-plaintext")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /auth/login, positive cases
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/login — success")
    class LoginSuccessTests {

        @Test
        @DisplayName("returns HTTP 200 when valid credentials are provided")
        void login_validCredentials_returns200() throws Exception {
            // Verifies the happy path returns 200 OK when all services succeed
            when(userService.login("alice", "correctpass")).thenReturn(testUser);
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token-abc");
            when(refService.createAndStoreRefreshToken(testUser)).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("response body contains the JWT string from jwtUtil.generateToken()")
        void login_responseBodyContainsJwt() throws Exception {
            // The JWT is returned in the response body so the frontend can store it in memory
            when(userService.login("alice", "correctpass")).thenReturn(testUser);
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token-abc");
            when(refService.createAndStoreRefreshToken(testUser)).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().string("jwt-token-abc"));
        }

        @Test
        @DisplayName("response headers contain Set-Cookie with the refresh token cookie")
        void login_responseContainsSetCookieHeader() throws Exception {
            // The refresh token is sent as an httpOnly cookie, not in the body
            when(userService.login("alice", "correctpass")).thenReturn(testUser);
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token-abc");
            when(refService.createAndStoreRefreshToken(testUser)).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().string("Set-Cookie", containsString("refresh_token")));
        }

        @Test
        @DisplayName("userService.login() is called with the correct username and password")
        void login_verifiesUserServiceCalled() throws Exception {
            // Ensures the controller passes request fields to the service correctly
            when(userService.login("alice", "correctpass")).thenReturn(testUser);
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token-abc");
            when(refService.createAndStoreRefreshToken(testUser)).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).login("alice", "correctpass");
        }

        @Test
        @DisplayName("refService.createAndStoreRefreshToken() is called with the logged-in user")
        void login_verifiesRefServiceCalled() throws Exception {
            // Ensures a refresh token is created and stored for the authenticated user
            when(userService.login("alice", "correctpass")).thenReturn(testUser);
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token-abc");
            when(refService.createAndStoreRefreshToken(testUser)).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(refService).createAndStoreRefreshToken(testUser);
        }

        @Test
        @DisplayName("jwtUtil.generateToken() is called with the logged-in user")
        void login_verifiesJwtUtilCalled() throws Exception {
            // Ensures a JWT is generated for the correct user, not a stale or default one
            when(userService.login("alice", "correctpass")).thenReturn(testUser);
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token-abc");
            when(refService.createAndStoreRefreshToken(testUser)).thenReturn(testCookie);

            LoginRequest request = new LoginRequest("alice", "correctpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(jwtUtil).generateToken(testUser);
        }
    }

    // -------------------------------------------------------------------------
    // POST /auth/login, negative cases
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/login — failure")
    class LoginFailureTests {

        @Test
        @DisplayName("returns HTTP 401 when user is not found")
        void login_userNotFound_returns401() throws Exception {
            // Controller should catch the service exception and return 401, not 500
            when(userService.login("ghost", "pass"))
                    .thenThrow(new IllegalArgumentException("User not found"));

            LoginRequest request = new LoginRequest("ghost", "pass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("returns HTTP 401 when password is incorrect")
        void login_invalidPassword_returns401() throws Exception {
            // Wrong password should produce the same 401 status as user-not-found
            when(userService.login("alice", "wrongpass"))
                    .thenThrow(new IllegalArgumentException("Invalid password"));

            LoginRequest request = new LoginRequest("alice", "wrongpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("response body contains the actual exception message, not a generic error")
        void login_failure_bodyContainsExceptionMessage() throws Exception {
            // The frontend needs the specific error message for user feedback
            when(userService.login("alice", "wrongpass"))
                    .thenThrow(new IllegalArgumentException("Invalid password"));

            LoginRequest request = new LoginRequest("alice", "wrongpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().string("Invalid password"));
        }

        @Test
        @DisplayName("refService.createAndStoreRefreshToken() is never called when login fails")
        void login_failure_refServiceNeverCalled() throws Exception {
            // No refresh token should be created if authentication failed
            when(userService.login("alice", "wrongpass"))
                    .thenThrow(new IllegalArgumentException("Invalid password"));

            LoginRequest request = new LoginRequest("alice", "wrongpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(refService, never()).createAndStoreRefreshToken(any());
        }

        @Test
        @DisplayName("jwtUtil.generateToken() is never called when login fails")
        void login_failure_jwtUtilNeverCalled() throws Exception {
            // No JWT should be generated if the user could not be authenticated
            when(userService.login("alice", "wrongpass"))
                    .thenThrow(new IllegalArgumentException("Invalid password"));

            LoginRequest request = new LoginRequest("alice", "wrongpass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil, never()).generateToken(any());
        }
    }

    // -------------------------------------------------------------------------
    // POST /auth/refresh, positive cases
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/refresh — success")
    class RefreshSuccessTests {

        @Test
        @DisplayName("returns HTTP 200 when a valid refresh_token cookie is present")
        void refresh_validCookie_returns200() throws Exception {
            // Happy path: valid cookie triggers token rotation and new JWT issuance
            when(refService.getUserByRefreshToken("old-token")).thenReturn(testUser);
            when(refService.rotateRefreshToken(testUser, "old-token")).thenReturn(testCookie);
            when(jwtUtil.generateToken(testUser)).thenReturn("new-jwt");

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "old-token")))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("response body contains the new JWT string")
        void refresh_responseBodyContainsNewJwt() throws Exception {
            // The rotated JWT is returned in the body for the frontend to store
            when(refService.getUserByRefreshToken("old-token")).thenReturn(testUser);
            when(refService.rotateRefreshToken(testUser, "old-token")).thenReturn(testCookie);
            when(jwtUtil.generateToken(testUser)).thenReturn("new-jwt");

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "old-token")))
                    .andExpect(content().string("new-jwt"));
        }

        @Test
        @DisplayName("response headers contain a rotated Set-Cookie header")
        void refresh_responseContainsRotatedCookie() throws Exception {
            // The old refresh token is replaced; new one is sent via Set-Cookie
            when(refService.getUserByRefreshToken("old-token")).thenReturn(testUser);
            when(refService.rotateRefreshToken(testUser, "old-token")).thenReturn(testCookie);
            when(jwtUtil.generateToken(testUser)).thenReturn("new-jwt");

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "old-token")))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().string("Set-Cookie", containsString("refresh_token")));
        }

        @Test
        @DisplayName("refService.getUserByRefreshToken() is called with the cookie value")
        void refresh_verifiesGetUserByRefreshTokenCalled() throws Exception {
            // Ensures the controller passes the raw cookie value to the service for lookup
            when(refService.getUserByRefreshToken("old-token")).thenReturn(testUser);
            when(refService.rotateRefreshToken(testUser, "old-token")).thenReturn(testCookie);
            when(jwtUtil.generateToken(testUser)).thenReturn("new-jwt");

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "old-token")))
                    .andExpect(status().isOk());

            verify(refService).getUserByRefreshToken("old-token");
        }

        @Test
        @DisplayName("refService.rotateRefreshToken() is called with the correct user and cookie")
        void refresh_verifiesRotateRefreshTokenCalled() throws Exception {
            // Rotation must receive both the user and the original cookie for delete + create
            when(refService.getUserByRefreshToken("old-token")).thenReturn(testUser);
            when(refService.rotateRefreshToken(testUser, "old-token")).thenReturn(testCookie);
            when(jwtUtil.generateToken(testUser)).thenReturn("new-jwt");

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "old-token")))
                    .andExpect(status().isOk());

            verify(refService).rotateRefreshToken(testUser, "old-token");
        }

        @Test
        @DisplayName("jwtUtil.generateToken() is called with the correct user")
        void refresh_verifiesJwtUtilCalled() throws Exception {
            // The new JWT must be issued for the user who owns the refresh token
            when(refService.getUserByRefreshToken("old-token")).thenReturn(testUser);
            when(refService.rotateRefreshToken(testUser, "old-token")).thenReturn(testCookie);
            when(jwtUtil.generateToken(testUser)).thenReturn("new-jwt");

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "old-token")))
                    .andExpect(status().isOk());

            verify(jwtUtil).generateToken(testUser);
        }
    }

    // -------------------------------------------------------------------------
    // POST /auth/refresh, negative cases
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /auth/refresh — failure")
    class RefreshFailureTests {

        @Test
        @DisplayName("returns HTTP 401 when refService.getUserByRefreshToken() throws")
        void refresh_invalidToken_returns401() throws Exception {
            // An invalid or expired refresh token should result in 401, not 500
            when(refService.getUserByRefreshToken("bad-token"))
                    .thenThrow(new IllegalArgumentException("Invalid refresh token"));

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "bad-token")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Invalid refresh token"));
        }

        @Test
        @DisplayName("returns HTTP 400 when no refresh_token cookie is present")
        void refresh_noCookie_returns400() throws Exception {
            // Spring throws MissingRequestCookieException before the controller runs,
            // resulting in a 400 Bad Request — no cookie means the request is malformed
            mockMvc.perform(post("/auth/refresh"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("refService.rotateRefreshToken() is never called when token lookup fails")
        void refresh_invalidToken_rotateNeverCalled() throws Exception {
            // If the token doesn't resolve to a user, rotation must not proceed
            when(refService.getUserByRefreshToken("bad-token"))
                    .thenThrow(new IllegalArgumentException("Invalid refresh token"));

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "bad-token")))
                    .andExpect(status().isUnauthorized());

            verify(refService, never()).rotateRefreshToken(any(), any());
        }

        @Test
        @DisplayName("jwtUtil.generateToken() is never called when token lookup fails")
        void refresh_invalidToken_jwtUtilNeverCalled() throws Exception {
            // No JWT should be issued if the refresh token is invalid
            when(refService.getUserByRefreshToken("bad-token"))
                    .thenThrow(new IllegalArgumentException("Invalid refresh token"));

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "bad-token")))
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil, never()).generateToken(any());
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
            // Happy path: cookie resolves to a user, tokens get revoked, cookie cleared
            when(refService.getUserByRefreshToken("good-token")).thenReturn(testUser);

            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "good-token")))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns HTTP 200 even when no refresh_token cookie is present")
        void logout_noCookie_returns200() throws Exception {
            // Logout must be idempotent — missing cookie still succeeds (no-op server side)
            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns HTTP 200 even when the refresh_token cookie is invalid")
        void logout_invalidCookie_returns200() throws Exception {
            // Invalid/expired token shouldn't fail logout — client still gets cookie cleared
            when(refService.getUserByRefreshToken("bad-token"))
                    .thenThrow(new IllegalArgumentException("Invalid refresh token"));

            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "bad-token")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("response headers contain an expired Set-Cookie clearing refresh_token")
        void logout_responseContainsExpiredCookie() throws Exception {
            // The response must carry a Set-Cookie with Max-Age=0 to clear the browser cookie
            when(refService.getUserByRefreshToken("good-token")).thenReturn(testUser);

            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "good-token")))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().string("Set-Cookie", containsString("refresh_token")))
                    .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
        }

        @Test
        @DisplayName("userService.logout() is called with the user resolved from the cookie")
        void logout_verifiesUserServiceLogoutCalled() throws Exception {
            // Ensures the controller resolves the user and delegates revocation to UserService
            when(refService.getUserByRefreshToken("good-token")).thenReturn(testUser);

            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "good-token")))
                    .andExpect(status().isOk());

            verify(userService).logout(testUser);
        }

        @Test
        @DisplayName("userService.logout() is never called when no cookie is present")
        void logout_noCookie_userServiceNeverCalled() throws Exception {
            // Without a cookie there is no user to revoke — logout must short-circuit
            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isOk());

            verify(userService, never()).logout(any());
        }

        @Test
        @DisplayName("userService.logout() is never called when cookie lookup fails")
        void logout_invalidCookie_userServiceNeverCalled() throws Exception {
            // If the token cannot be resolved to a user, revocation must not proceed
            when(refService.getUserByRefreshToken("bad-token"))
                    .thenThrow(new IllegalArgumentException("Invalid refresh token"));

            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "bad-token")))
                    .andExpect(status().isOk());

            verify(userService, never()).logout(any());
        }
    }
}
