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

/**
 * Utility component for handling JSON Web Tokens (JWT).
 * <p>
 * This class provides methods for generating, parsing, and validating JWTs used for 
 * stateless authentication. It manages the cryptographic signing key and defines 
 * the structure of the token payload, including custom claims for organizational 
 * context and user identity.
 * </p>
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private SecretKey key;

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Decodes the Base64 encoded secret and generates a HMAC SHA key.
     * * @return a {@link SecretKey} for signing and verifying tokens.
     */
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Initializes the signing key after the component has been constructed and 
     * dependencies injected.
     */
    @PostConstruct
    private void init() {
        this.key = getKey();
    }

    /**
     * Generates a signed JWT for a specific user and their organizational context.
     * <p>
     * The token includes the user's ID as the subject and custom claims for:
     * <ul>
     * <li>Legal Name</li>
     * <li>Role (from {@link OrgUserBridge})</li>
     * <li>Organization ID</li>
     * <li>Email</li>
     * </ul>
     * Tokens are configured with a 15-minute expiration window.
     * </p>
     *
     * @param user   the user for whom the token is generated.
     * @param bridge the bridge entity defining the user's role and organization.
     * @return a compact, URL-safe JWT string, or {@code null} if generation fails.
     */
    public String generateToken(User user, OrgUserBridge bridge) {
        Date expiration = new Date(System.currentTimeMillis() + 15 * 60 * 1000);

        try {
            String jwt = Jwts.builder()
            .subject(user.getId().toString())
            .claim("legalName", user.getLegalName()) 
            .claim("role", bridge != null ? bridge.getUserRole().name() : null)
            .claim("orgId", bridge != null ? bridge.getOrganization().getId() : null)
            .claim("email", user.getEmail())
            .issuedAt(new Date())
            .expiration(expiration)
            .signWith(key)
            .compact();
            
            logger.info("Successfully generated JWT token");
            return jwt;
        } catch (JwtException error) {
            logger.error("Error creating JWT: {}", error.getMessage());
            return null;
        }
    }

    /**
     * Validates a JWT string by attempting to parse its claims.
     *
     * @param jwtToken the raw JWT string.
     * @return {@code true} if the token is well-formed, correctly signed, and not expired; 
     * {@code false} otherwise.
     */
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

    /**
     * Decrypts and parses the JWT payload using the configured secret key.
     *
     * @param jwtToken the raw JWT string.
     * @return the {@link Claims} object containing the token payload.
     * @throws JwtException if the token is invalid or the signature fails verification.
     */
    public Claims extractAllClaims(String jwtToken) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(jwtToken)
            .getPayload();
    }

    /**
     * Extracts the User ID from the token subject.
     * * @param jwtToken the raw JWT string.
     * @return the User ID as a {@link Long}.
     */
    public Long extractUserId(String jwtToken) {
        return Long.parseLong(extractAllClaims(jwtToken).getSubject());
    }

    /**
     * Extracts the user's legal name claim.
     * * @param jwtToken the raw JWT string.
     * @return the legal name stored in the token.
     */
    public String extractLegalName(String jwtToken) {
        return extractAllClaims(jwtToken).get("legalName", String.class);
    }

    /**
     * Extracts the user's role claim.
     * * @param jwtToken the raw JWT string.
     * @return the role name stored in the token.
     */
    public String extractUserRole(String jwtToken) {
        return extractAllClaims(jwtToken).get("role", String.class);
    }

    /**
     * Extracts the organization ID claim.
     * * @param jwtToken the raw JWT string.
     * @return the organization ID as a {@link Long}.
     */
    public Long extractUserOrgId(String jwtToken) {
        return extractAllClaims(jwtToken).get("orgId", Long.class);
    }

    /**
     * Retrieves the current user's ID from the global Spring Security context.
     * <p>
     * Note: This method assumes the {@link SecurityContextHolder} has been 
     * previously populated by an authentication filter.
     * </p>
     *
     * @return the authenticated user's ID.
     * @throws IllegalStateException if no authentication is present in the context.
     */
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

    /**
     * Extracts the user's email claim.
     * * @param jwtToken the raw JWT string.
     * @return the email address stored in the token.
     */
    public String extractUserEmail(String jwtToken) {
        return extractAllClaims(jwtToken).get("email", String.class);
    }
}