package com.grimni.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.User;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private SecretKey key;

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @PostConstruct
    private void init() {
        this.key = getKey();
    }

    public String generateToken(User user, OrgUserBridge bridge) {
        Date expiration = new Date(System.currentTimeMillis() + 15 * 60 * 1000);

        try {
            String jwt = Jwts.builder()
            .subject(user.getId().toString())
            .claim("legalName", user.getLegalName()) 
            .claim("role", bridge.getUserRole().name())
            .claim("orgId", bridge.getOrganization().getId())
            .issuedAt(new Date())
            .expiration(expiration)
            .signWith(key)
            .compact();
            
            logger.info("Succesfully generated jwt token");

            logger.info("Successfully generated JWT token");
            return jwt;
        } catch (JwtException error) {
            logger.error("Error creating JWT: {}", error.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.isEmpty()) {
                logger.error("JWT token is null or empty");
                return false;
            }
            extractAllClaims(jwtToken);
            return true;
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Claims extractAllClaims(String jwtToken) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(jwtToken)
            .getPayload();
    }

    public Long extractUserId(String jwtToken) {
        return Long.parseLong(extractAllClaims(jwtToken).getSubject());
    }

    public String extractLegalName(String jwtToken) {
        return extractAllClaims(jwtToken).get("legalName", String.class);
    }

    public String extractUserRole(String jwtToken) {
        return extractAllClaims(jwtToken).get("role", String.class);
    }

    public Long extractUserOrgId(String jwtToken) {
        return extractAllClaims(jwtToken).get("orgId", Long.class);
    }

    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Missing authenticated user");
        }

        String userId = authentication.getPrincipal().toString();
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid authenticated user id", exception);
        }
    }
}
