package com.grimni.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.grimni.domain.Organization;
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
     * Executes a secure token rotation initiated by a user.
     * <p>
     * This method validates that the token exists and belongs to the calling user, 
     * then delegates to the internal rotation logic to issue a new token.
     * </p>
     *
     * @param user          the user requesting the rotation.
     * @param incomingToken the current plaintext refresh token from the cookie.
     * @param organization  the organization context to associate with the new token.
     * @return a {@link ResponseCookie} containing the new plaintext token.
     * @throws BadCredentialsException if the token does not belong to the authenticated user.
     */
    public ResponseCookie rotateRefreshToken(User user, String incomingToken, Organization organization) {
        logger.info("Rotating refresh token for user: {}", user.getLegalName());
        RefreshToken existing = validateRefreshToken(incomingToken);
        if (!existing.getUser().getId().equals(user.getId())) {
            logger.warn("Refresh token rotation denied: token user {} does not match caller {}", existing.getUser().getId(), user.getId());
            throw new BadCredentialsException("Refresh token does not belong to the authenticated user");
        }

        return rotateRefreshToken(existing, organization);
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
     * Generates, hashes, and persists a new refresh token for the specified user and organization.
     *
     * <p>The plaintext token is generated via {@code SecureRandom}, hashed with SHA-256,
     * and stored in the database. Only the plaintext is returned to the client
     * via an {@code httpOnly} cookie — the database never stores the plaintext value.</p>
     *
     * @param user         the authenticated user to create the refresh token for.
     * @param organization the organization context for the current session.
     * @return a {@link ResponseCookie} containing the plaintext refresh token, scoped to {@code /api/auth}.
     * @throws IllegalArgumentException if the user is null.
    */
    public ResponseCookie createAndStoreRefreshToken(User user, Organization organization) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        logger.info("Creating and storing refresh token for user: {}", user.getLegalName());
        
        String plaintext = util.generateRefreshToken();
        String hashed = util.hashToken(plaintext);

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setOrganization(organization);
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

    /**
     * Retrieves the stored token metadata for a given plaintext token.
     *
     * @param tokenValue the plaintext refresh token.
     * @return the verified {@link RefreshToken} entity.
     */
    public RefreshToken getStoredRefreshToken(String tokenValue) {
        return validateRefreshToken(tokenValue);
    }

    /**
     * Validates and retrieves a refresh token specifically for a target user ID.
     *
     * @param tokenValue the plaintext refresh token.
     * @param userId     the ID of the user who must own the token.
     * @return the verified {@link RefreshToken} entity.
     * @throws BadCredentialsException if the token exists but belongs to a different user.
     */
    public RefreshToken getStoredRefreshTokenForUser(String tokenValue, Long userId) {
        RefreshToken storedToken = validateRefreshToken(tokenValue);
        if (!storedToken.getUser().getId().equals(userId)) {
            logger.warn("Refresh token lookup denied: token user {} does not match authenticated user {}", storedToken.getUser().getId(), userId);
            throw new BadCredentialsException("Refresh token does not belong to the authenticated user");
        }

        return storedToken;
    }

    /**
     * Internal method to perform token rotation by entity.
     * <p>
     * Deletes the existing database record and issues a new one, ensuring the 
     * "Single Use" nature of refresh tokens is enforced.
     * </p>
     *
     * @param existingToken the database entity of the token to be rotated.
     * @param organization  the organization context to carry over or update.
     * @return a {@link ResponseCookie} containing the new plaintext token.
     */
    public ResponseCookie rotateRefreshToken(RefreshToken existingToken, Organization organization) {
        logger.info("Rotating refresh token for user: {}", existingToken.getUser().getLegalName());
        repository.delete(existingToken);
        logger.info("Old refresh token deleted for user: {}", existingToken.getUser().getLegalName());
        return createAndStoreRefreshToken(existingToken.getUser(), organization);
    }
}