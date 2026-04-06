package com.grimni.security;

import java.io.Serializable;

import org.springframework.security.core.AuthenticatedPrincipal;

public record JwtUserPrinciple(
        Long userId,
        Long orgId,
        String username,
        String role) implements AuthenticatedPrincipal, Serializable {

        @Override
        public String getName() {
            return username;
        }
}
