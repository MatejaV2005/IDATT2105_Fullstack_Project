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

/**
 * Service for managing the lifecycle of Refresh Tokens used in JWT-based authentication.
 * <p>
 * This service implements secure token handling patterns, including:
 * <ul>
 * <li><b>Token Rotation:</b> Invalidating old tokens and issuing new ones upon refresh to mitigate replay attacks.</li>
 * <li><b>One-Way Hashing:</b> Storing only SHA-256 hashes in the database to prevent token theft in the event of a DB breach.</li>
 * <li><b>Revocation:</b> Support for global logout by wiping all tokens associated with a user.</li>
 * <li><b>HTTP-Only Cookies:</b> Leveraging {@link RefreshTokenUtil} to package plaintext tokens in secure cookies.</li>
 * </ul>
 */
@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private final RefreshTokenUtil util;
    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenUtil util, RefreshTokenRepository repository) {
        this.util = util;
        this.repository = repository;
    }

    /**
     * Validates the existence and integrity of an incoming refresh token.
     *
     * @param incomingToken the raw plaintext token from the client cookie.
     * @return the {@link RefreshToken} entity if found.
     * @throws BadCredentialsException if the token hash does not match any record in the database.
     */
    private RefreshToken validateRefreshToken(String incomingToken) {
        String hashed = util.hashToken(incomingToken);
        return repository.findByRefreshToken(hashed)
                .orElseThrow(() -> {
                    logger.warn("Refresh token validation failed: token not found in database");
                    return new BadCredentialsException("Invalid refresh token");
                });
    }

    /**
     * Executes a secure token rotation.
     * <p>
     * This method validates the provided token, deletes it from the database to prevent reuse,
     * and issues a brand-new refresh token for the user. This is the primary mechanism for
     * maintaining long-lived sessions securely.
     * </p>
     *
     * @param user          the user requesting the rotation.
     * @param incomingToken the current plaintext refresh token.
     * @return a {@link ResponseCookie} containing the new plaintext token.
     */
    public ResponseCookie rotateRefreshToken(User user, String incomingToken) {
        logger.info("Rotating refresh token for user: {}", user.getLegalName());
        RefreshToken existing = validateRefreshToken(incomingToken); 

        repository.delete(existing);

        logger.info("Old refresh token deleted for user: {}", user.getLegalName());
        return createAndStoreRefreshToken(user);
    }

    /**
     * Revokes all active refresh tokens for a specific user.
     * <p>
     * Primarily used during logout or security resets to ensure all sessions across
     * all devices are terminated.
     * </p>
     *
     * @param user the user whose tokens should be revoked.
     */
    public void revokeAllTokens(User user) {
        logger.info("Revoking all refresh tokens for user: {}", user.getLegalName());
        repository.deleteByUser(user);
        logger.info("All refresh tokens revoked for user: {}", user.getLegalName());
    }

    /**
     * Generates, hashes, and persists a new refresh token for the specified user.
     *
     * <p>The plaintext token is generated via {@code SecureRandom}, hashed with SHA-256,
     * and stored in the database. Only the plaintext is returned to the client
     * via an {@code httpOnly} cookie — the database never stores the plaintext value.</p>
     *
     * <p><b>Rolling expiry:</b> Each call issues a new cookie with a fresh 7-day {@code maxAge}.
     * This means active users will have their session window extended on every JWT refresh
     * cycle (approx. every 15 min). Sessions only expire after 7 consecutive days of inactivity.
     * This design prioritizes seamless usability over strict session termination.</p>
     *
     * @param user the authenticated user to create the refresh token for.
     * @return a {@link ResponseCookie} containing the plaintext refresh token, scoped to {@code /api/auth}.
     * @throws IllegalArgumentException if the user is null.
    */
    public ResponseCookie createAndStoreRefreshToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        logger.info("Creating and storing refresh token for user: {}", user.getLegalName());
        
        String plaintext = util.generateRefreshToken();
        String hashed = util.hashToken(plaintext);

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setRefreshToken(hashed);
        repository.save(entity);
        logger.info("Refresh token stored in database for user: {}", user.getLegalName());

        return util.createRefreshTokenCookie(plaintext);
    }

    /**
     * Retrieves the owner of a refresh token after successful validation.
     *
     * @param tokenValue the plaintext refresh token value.
     * @return the {@link User} associated with the token.
     */
    public User getUserByRefreshToken(String tokenValue) {
        RefreshToken refToken = validateRefreshToken(tokenValue);
        return refToken.getUser();
    }
}