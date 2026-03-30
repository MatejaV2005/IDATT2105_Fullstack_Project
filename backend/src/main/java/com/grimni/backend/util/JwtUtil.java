package com.grimni.backend.util;
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
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
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

    //TODO: extend for adding subject claims that add roles to payload
    public String generateToken(String username) {
        Date expiration= new Date(System.currentTimeMillis() + 15 * 60 * 1000); // 15 minute token expiry limit
        
        try {
            String jwt = Jwts.builder()
            .subject(username)
            .expiration(expiration)
            .signWith(key).compact();
            
            logger.info("Succesfully generated jwt token");

            return jwt;
        } catch(JwtException error) {
            logger.error("error creating jwt: " + error);
            return null;
        }
    }

    
    public boolean isTokenValid(String jwtToken) {
        try {
            // if parsed token doesn't throw any exception when validating with secretKey, return true
            Jwts.parser().verifyWith(key).build().parseSignedClaims(jwtToken);
            logger.info("jwtToken is valid");
            return true;
        } catch(JwtException e) {
            logger.error("ERROR: jwtToken is not valid");
            return false; // otherwise false
        }
    }

    
    public String extractUsername(String JwtToken) {
        try {
            return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(JwtToken)
            .getPayload()
            .getSubject();
        } catch (JwtException error) {
            logger.error("Error parsing and extracting username: " + error);
            return null;
        }
    }

    //TODO: add extractUserId, extractUserRole, and extractUserOrgId when ready to do so    
}
