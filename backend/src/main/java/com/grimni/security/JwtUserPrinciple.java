package com.grimni.security;

import java.io.Serializable;

import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * Custom security principal representing an authenticated user within a JWT-based session.
 * <p>
 * This record serves as the "identity" object stored in the Spring Security context. 
 * It encapsulates essential user metadata extracted from the JWT claims, specifically 
 * linking the user to their primary organization and assigned role. 
 * </p>
 * <p>
 * Implements {@link AuthenticatedPrincipal} for Spring Security compatibility and 
 * {@link Serializable} to support potential session persistence or distributed caching.
 * </p>
 * * @param userId   The unique database identifier of the user.
 * @param orgId    The identifier of the organization the user is currently authenticated within.
 * @param username The display name or legal name of the user.
 * @param role     The organizational role assigned to the user (e.g., OWNER, MANAGER).
 */
public record JwtUserPrinciple(
        Long userId,
        Long orgId,
        String username,
        String role) implements AuthenticatedPrincipal, Serializable {

        /**
         * Returns the name of the principal, required by the {@link AuthenticatedPrincipal} interface.
         * * @return the username/legal name of the authenticated user.
         */
        @Override
        public String getName() {
            return username;
        }
}