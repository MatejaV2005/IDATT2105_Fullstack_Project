package com.grimni.service;

import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.grimni.domain.RefreshToken;
import com.grimni.domain.User;
import com.grimni.repository.RefreshTokenRepository;
import com.grimni.util.RefreshTokenUtil;

@Service
public class RefreshTokenService {
    private final RefreshTokenUtil util;
    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenUtil util, RefreshTokenRepository repository) {
        this.util = util;
        this.repository = repository;
    }

    // 1. method for validating a refresh token
    public boolean validateRefreshToken(User user, String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        List<RefreshToken> tokens = user.getRefreshTokens();

        for (RefreshToken tok : tokens) {
            if (tok.getTokenValue().equals(token)) {
                return true;
            }
        }
        return false;
    }

    // 2. method for sending new refToken upon expiry of JWT token
    // Rotates the refresh token: deletes the old one, issues a new one, returns a new cookie
    public ResponseCookie rotateRefreshToken(User user, String incomingToken) {
        String hashed = util.hashToken(incomingToken);

        RefreshToken existing = repository.findByTokenValue(hashed)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        repository.delete(existing);

        return createAndStoreRefreshToken(user);
    }

    // 3. method for deleting tokens upon expiry/logout from database
    // Revokes all refresh tokens for a user (logout / security wipe)
    public void revokeAllTokens(User user) {
        repository.deleteByUser(user);
    }

    // 4. method for generating the refToken, hashing it and saving it per user in the database
    // will handle the business logic and flow of generating token, hashing it, creating the cookie from token and storing in DB

    public ResponseCookie createAndStoreRefreshToken(User user) {
        // generate a random 32-byte token from RandomSecure, and hash the token
        String plaintext = util.generateRefreshToken();
        String hashed = util.hashToken(plaintext);
    
        // Create a new RefreshToken object for a specific user, set necessary fields and store it in database through JPA repository
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenValue(hashed);
        entity.setCreatedAt(new Date());
        repository.save(entity);
    
        // return ResponseCookie object for being used in Http requests
        return util.createRefreshTokenCookie(plaintext);
    }
}
