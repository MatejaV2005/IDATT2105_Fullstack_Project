package com.grimni.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.User;
import com.grimni.dto.LoginRequest;
import com.grimni.service.RefreshTokenService;
import com.grimni.service.UserService;
import com.grimni.util.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final RefreshTokenService refService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(RefreshTokenService refService, JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.refService = refService;
        this.userService = userService;
    }

    private OrgUserBridge resolveBridge(User user, Long orgId) {
        return user.getOrganizations().stream()
                .filter(b -> b.getOrganization().getId().equals(orgId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "User is not a member of organization " + orgId));
    }

    /*MENTION IN JAVADOC, POST and not GET due to REST state idempotency*/

    // TODO: Redundant DB hit — getUserByRefreshToken and rotateRefreshToken both call
    // validateRefreshToken internally. Refactor using RotationResult record to eliminate
    // duplicate hash + DB lookup in the refresh flow. See RefreshTokenService.rotateRefreshToken.
    @PostMapping("/refresh")
    public ResponseEntity<?> grantRefreshToken(@CookieValue("refresh_token") String cookie, @RequestBody Long orgId) {
        try {
            logger.info("Refresh token request received");

            User user = refService.getUserByRefreshToken(cookie);
            ResponseCookie refToken = refService.rotateRefreshToken(user, cookie);
            OrgUserBridge bridge = resolveBridge(user, orgId);
            String jwtToken = jwtUtil.generateToken(user, bridge);

            // return status 200 OK + set the rotated refresh token in header and new JWT token in body
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refToken.toString()).body(jwtToken);
                            
            } catch(Exception error) {
                return ResponseEntity.status(401).body(error.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@CookieValue(value = "refresh_token", required = false) String cookie) {
        logger.info("Logout request received");
        try {
            if (cookie != null) {
                // removes the cookie from the database for the user upon logout
                User user = refService.getUserByRefreshToken(cookie);
                userService.logout(user);
            }
        } catch (Exception error) {
            logger.warn("Logout token lookup failed, clearing cookie anyway: {}", error.getMessage());
        }

        // sends a new RefreshToken cookie to the browser with 0 duration to clear the existing one in browser
        ResponseCookie expired = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, expired.toString()).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        logger.info("Login attempt for user: {}", request.email());
        try {
            User user = userService.login(request.email(), request.password());
            OrgUserBridge bridge = resolveBridge(user, request.orgId());
            String jwtToken = jwtUtil.generateToken(user, bridge);

            ResponseCookie refToken = refService.createAndStoreRefreshToken(user);

            logger.info("Login successful for user: {}", request.email());
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refToken.toString()).body(jwtToken);

        } catch(Exception error) {
            logger.warn("Login failed for user '{}': {}", request.email(), error.getMessage());
            return ResponseEntity.status(401).body(error.getMessage());
        }
    } 
}
