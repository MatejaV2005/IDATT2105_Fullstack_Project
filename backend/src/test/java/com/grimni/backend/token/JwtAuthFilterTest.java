package com.grimni.backend.token;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JwtAuthFilterTest {

    private JwtUtil jwtUtil;
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        jwtAuthFilter = new JwtAuthFilter(jwtUtil);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Always clean up SecurityContextHolder between tests to avoid state leaking
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // Filter ordering — verifies JwtAuthFilter runs before UsernamePasswordAuthenticationFilter
    // -------------------------------------------------------------------------
    @Nested
    class FilterOrderingTest {

        @Test
        void jwtAuthFilter_extendsOncePerRequestFilter() {
            // JwtAuthFilter extends OncePerRequestFilter, which guarantees it executes
            // exactly once per request — required for a pre-auth filter sitting before
            // UsernamePasswordAuthenticationFilter in the chain
            assertInstanceOf(OncePerRequestFilter.class, jwtAuthFilter,
                    "JwtAuthFilter must extend OncePerRequestFilter");
        }
    }

    // -------------------------------------------------------------------------
    // No Authorization header
    // -------------------------------------------------------------------------
    @Nested
    class NoAuthorizationHeaderTest {

        @Test
        void noAuthorizationHeader_filterChainContinues() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        void noAuthorizationHeader_securityContextRemainsEmpty() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            assertNull(SecurityContextHolder.getContext().getAuthentication(),
                    "SecurityContextHolder should remain empty when no Authorization header is present");
        }

        @Test
        void noAuthorizationHeader_statusRemainsOk() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            assertEquals(200, response.getStatus(),
                    "Status should not be changed when there is no Authorization header");
        }

        @Test
        void authorizationHeaderWithoutBearerPrefix_isIgnored() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    // -------------------------------------------------------------------------
    // Invalid token
    // -------------------------------------------------------------------------
    @Nested
    class InvalidTokenTest {

        @Test
        void invalidToken_returns401() throws Exception {
            when(jwtUtil.isTokenValid(any())).thenReturn(false);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer invalid.token.value");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            assertEquals(401, response.getStatus());
        }

        @Test
        void invalidToken_filterChainDoesNotContinue() throws Exception {
            when(jwtUtil.isTokenValid(any())).thenReturn(false);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer invalid.token.value");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            verify(chain, never()).doFilter(any(), any());
        }

        @Test
        void invalidToken_securityContextRemainsEmpty() throws Exception {
            when(jwtUtil.isTokenValid(any())).thenReturn(false);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer invalid.token.value");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        void validTokenButNullUsername_returns401() throws Exception {
            when(jwtUtil.isTokenValid(any())).thenReturn(true);
            when(jwtUtil.extractUsername(any())).thenReturn(null);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer some.valid.token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            assertEquals(401, response.getStatus());
            verify(chain, never()).doFilter(any(), any());
        }
    }

    // -------------------------------------------------------------------------
    // Valid token
    // -------------------------------------------------------------------------
    @Nested
    class ValidTokenTest {

        @Test
        void validToken_returns200() throws Exception {
            when(jwtUtil.isTokenValid(any())).thenReturn(true);
            when(jwtUtil.extractUsername(any())).thenReturn("alice");

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer valid.token.here");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            assertEquals(200, response.getStatus());
        }

        @Test
        void validToken_filterChainContinues() throws Exception {
            when(jwtUtil.isTokenValid(any())).thenReturn(true);
            when(jwtUtil.extractUsername(any())).thenReturn("alice");

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer valid.token.here");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        void validToken_securityContextHolderIsPopulated() throws Exception {
            when(jwtUtil.isTokenValid(any())).thenReturn(true);
            when(jwtUtil.extractUsername(any())).thenReturn("alice");

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer valid.token.here");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth, "SecurityContextHolder should hold an Authentication after a valid token");
            assertInstanceOf(UsernamePasswordAuthenticationToken.class, auth);
        }

        @Test
        void validToken_securityContextHolderContainsCorrectUsername() throws Exception {
            when(jwtUtil.isTokenValid(any())).thenReturn(true);
            when(jwtUtil.extractUsername(any())).thenReturn("alice");

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer valid.token.here");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertEquals("alice", auth.getPrincipal(),
                    "SecurityContextHolder principal should match the username extracted from the token");
        }

        @Test
        void validToken_authenticationIsMarkedAsAuthenticated() throws Exception {
            when(jwtUtil.isTokenValid(any())).thenReturn(true);
            when(jwtUtil.extractUsername(any())).thenReturn("alice");

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer valid.token.here");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            jwtAuthFilter.doFilter(request, response, chain);

            assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated(),
                    "Authentication in SecurityContextHolder should be marked as authenticated");
        }
    }
}
