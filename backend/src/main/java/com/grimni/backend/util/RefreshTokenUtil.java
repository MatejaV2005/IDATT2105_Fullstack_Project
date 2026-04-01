package com.grimni.backend.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Component
public class RefreshTokenUtil {
    
    public String generateRefreshToken() {
        byte[] bytearr = new byte[32];
        new SecureRandom().nextBytes(bytearr); // fills the byte array random bytes with the operating systems entropy source

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytearr); // converts it into string of readable ASCII values
    }

    public void setHttpOnly(String tokenValue) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokenValue)
                                .httpOnly(true) // protects from XSS, making it httpOnly
                                .secure(true) // can only be sent over HTTPS
                                .maxAge(Duration.ofDays(7)) // expires after 7 days
                                .build();
    }
}
