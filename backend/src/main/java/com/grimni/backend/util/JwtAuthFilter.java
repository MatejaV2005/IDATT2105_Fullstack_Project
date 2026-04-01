package com.grimni.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.jspecify.annotations.NonNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

// OncePerRequestFilter guarantees that this filter is executed exactly once per HTTP request
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7); // starts the string at index 7 of the original string

            if (!jwtUtil.isTokenValid(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                logger.error("Invalid JWT Token, Unauthorized to resume further");
                return;
            }

            String username = jwtUtil.extractUsername(jwtToken);

            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                logger.error("Could not extract username from token");
                return;
            }
            
            // Class in spring security that represents an authentication object (in this case user)
            // TODO: in third parameter, ensure that the empty list will hold roles for authenticated user 
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            
            // SecurityContextHolder is a global context storage, storing the authenticated entity for use in the filterchain
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("token validated, pass response and request to next filter in chain");
        }

        filterChain.doFilter(request, response); //Pass this custom filter to next filter in filterchain
    }
}
