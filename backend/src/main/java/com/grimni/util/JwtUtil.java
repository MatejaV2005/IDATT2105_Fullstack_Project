package com.grimni.util;
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
import org.springframework.stereotype.Component;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.User;


@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private SecretKey key;

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Decodes the base64-encoded secret from application properties and constructs
     * an HMAC-SHA key suitable for signing and verifying JWT tokens.
     *
     * @return a {@link SecretKey} derived from the configured JWT secret
     */
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret); // decodes the base64 encoded secret key to an array of raw bytes
        return Keys.hmacShaKeyFor(keyBytes); // wraps into a SecretKey object for type correspondance with JJWT API
    }

    @PostConstruct // tells spring to call this injection after all other are finished when creating JwtUtil bean
    private void init() {
        this.key = getKey();
    }



    /**
     * Generates a signed JWT token with the given username as subject, valid for 5 minutes.
     * @param username the subject to embed in the token
     * @return a signed JWT string
     */

    public String generateToken(User user, OrgUserBridge bridge) {
        Date expiration= new Date(System.currentTimeMillis() + 15 * 60 * 1000); // 15 minute token expiry limit
        
        try {
            String jwt = Jwts.builder()
            .subject(user.getId().toString())
            .claim("username", user.getLegalName()) // ? Wallah
            .claim("role", bridge.getUserRole().name())
            .claim("orgId", bridge.getOrganization().getId())
            .issuedAt(new Date())
            .expiration(expiration)
            .signWith(key)
            .compact();
            
            logger.info("Succesfully generated jwt token");

            return jwt;
        } catch(JwtException error) {
            logger.error("error creating jwt: " + error);
            return null;
        }
    }

    
    public boolean isTokenValid(String jwtToken) {
        try {
            
            if (jwtToken == null || jwtToken.isEmpty()) {
                logger.error("Error: JWT token is null or empty");
                return false;
            }

            // if parsed token doesn't throw any exception when validating with secretKey, return true
            Jwts.parser().verifyWith(key).build().parseSignedClaims(jwtToken);
            logger.info("jwtToken is valid");
            return true;
        } catch(JwtException e) {
            logger.error("ERROR: jwtToken is not valid");
            return false; // otherwise false
        }
    }

    
    public String extractUserId(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.isEmpty()) {
                logger.error("Error: JWT token is null or empty");
                return null;
            }

            return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(jwtToken)
            .getPayload()
            .getSubject();

        } catch (JwtException error) {
            logger.error("Error parsing and extracting user id: " + error);
            return null;
        }
    }


    public String extractUsername(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.isEmpty()) {
                logger.error("Error: JWT token is null or empty");
                return null;
            }

            return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(jwtToken)
            .getPayload()
            .get("username", String.class);

        } catch (JwtException error) {
            logger.error("Error parsing and extracting username: " + error);
            return null;
        }
    }

    public String extractUserRole(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.isEmpty()) {
                logger.error("Error: JWT token is null or empty");
                return null;
            }

            return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(jwtToken)
            .getPayload()
            .get("role", String.class);

        } catch (JwtException error) {
            logger.error("Error parsing and extracting user role: " + error);
            return null;
        }
    }

    public Long extractUserOrgId(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.isEmpty()) {
                logger.error("Error: JWT token is null or empty");
                return null;
            }

            return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(jwtToken)
            .getPayload()
            .get("orgId", Long.class);

        } catch (JwtException error) {
            logger.error("Error parsing and extracting organization ID: " + error);
            return null;
        }
    }
}
