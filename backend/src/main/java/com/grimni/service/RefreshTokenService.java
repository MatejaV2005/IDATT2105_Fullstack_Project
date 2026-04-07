package com.grimni.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.grimni.domain.RefreshToken;
import com.grimni.domain.User;
import com.grimni.repository.RefreshTokenRepository;
import com.grimni.util.RefreshTokenUtil;

import org.springframework.security.authentication.BadCredentialsException;

@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private final RefreshTokenUtil util;
    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenUtil util, RefreshTokenRepository repository) {
        this.util = util;
        this.repository = repository;
    }

    // 1. Private validation concern, single responsibility: confirm token exists in DB
    private RefreshToken validateRefreshToken(String incomingToken) {
        String hashed = util.hashToken(incomingToken);
        return repository.findByRefreshToken(hashed)
                .orElseThrow(() -> {
                    logger.warn("Refresh token validation failed: token not found in database");
                    return new BadCredentialsException("Invalid refresh token");
                });
    }

    // 2. Rotates the refresh token: validates, deletes old, issues new, returns new cookie
    // NOTE: POST and not GET due to REST state idempotency — rotation mutates DB state
    public ResponseCookie rotateRefreshToken(User user, String incomingToken) {
        logger.info("Rotating refresh token for user: {}", user.getLegalName());
        RefreshToken existing = validateRefreshToken(incomingToken); // reuse validation, single DB hit

        repository.delete(existing);

        logger.info("Old refresh token deleted for user: {}", user.getLegalName());
        return createAndStoreRefreshToken(user);
    }

    // 3. method for deleting tokens upon expiry/logout from database
    // Revokes all refresh tokens for a user (logout / security wipe)
    public void revokeAllTokens(User user) {
        logger.info("Revoking all refresh tokens for user: {}", user.getLegalName());
        repository.deleteByUser(user);
        logger.info("All refresh tokens revoked for user: {}", user.getLegalName());
    }

    // 4. method for generating the refToken, hashing it and saving it per user in the database
    // will handle the business logic and flow of generating token, hashing it, creating the cookie from token and storing in DB

    /**
     * Creates, hashes, stores, and returns a refresh token for the given user.
     *
     * <p>The plaintext token is generated via SecureRandom, hashed with SHA-256,
     * and stored in the database. Only the plaintext is returned to the client
     * via an httpOnly cookie — the database never stores the plaintext value.</p>
     *
     * <p><b>Rolling expiry:</b> Each call issues a new cookie with a fresh 7-day maxAge.
     * This means active users will have their session window extended on every JWT refresh
     * cycle (15 min). Sessions only expire after 7 consecutive days of inactivity.
     * This is an intentional design decision prioritizing usability over strict session
     * termination. For high-security contexts, consider absolute expiry based on
     * the original createdAt timestamp.</p>
     *
     * @param user the authenticated user to create the refresh token for
     * @return a ResponseCookie containing the plaintext refresh token, scoped to /api/auth
    */
    public ResponseCookie createAndStoreRefreshToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        logger.info("Creating and storing refresh token for user: {}", user.getLegalName());
        // generate a random 32-byte token from RandomSecure, and hash the token
        String plaintext = util.generateRefreshToken();
        String hashed = util.hashToken(plaintext);

        // Create a new RefreshToken object for a specific user, set necessary fields and store it in database through JPA repository
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setRefreshToken(hashed);
        entity.setOrgId(null);
        repository.save(entity);
        logger.info("Refresh token stored in database for user: {}", user.getLegalName());

        // return ResponseCookie object for being used in Http requests
        return util.createRefreshTokenCookie(plaintext);
    }

    public User getUserByRefreshToken(String tokenValue) {
        RefreshToken refToken = validateRefreshToken(tokenValue);
        return refToken.getUser();
    }
}
