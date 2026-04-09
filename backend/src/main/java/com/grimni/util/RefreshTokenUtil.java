package com.grimni.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Component
public class RefreshTokenUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    
    public String generateRefreshToken() {
        byte[] bytearr = new byte[32];
        new SecureRandom().nextBytes(bytearr); // fills the byte array random bytes with the operating systems entropy source

        logger.info("token value for refresh token created succesfully");
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytearr); // converts it into string of readable ASCII values
    }

    public ResponseCookie createRefreshTokenCookie(String tokenValue) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokenValue)
                                .httpOnly(true) // protects from XSS, making it httpOnly
                                .maxAge(Duration.ofDays(7)) // expires after 7 days
                                .path("/api/auth") // scopes cookie to only be sendt on /api/auth... endpoints
                                .sameSite("Strict") // protects from CSRF
                                .build();

        logger.info("refreshToken cookie created successfully");
        return cookie;
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); // returns a message digest object that implements the specified hash algorithm
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8)); // transforms the token into its correspondign array of bytes, and hashes w ith SHA-256
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes); // encodes the bytes into base64 and returns as string
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
