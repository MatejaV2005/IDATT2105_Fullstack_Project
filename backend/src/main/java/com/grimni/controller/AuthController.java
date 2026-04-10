package com.grimni.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.RefreshToken;
import com.grimni.domain.User;
import com.grimni.dto.LoginRequest;
import com.grimni.dto.RegisterRequest;
import com.grimni.dto.SwitchOrganizationRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.OrganizationService;
import com.grimni.service.RefreshTokenService;
import com.grimni.service.UserService;
import com.grimni.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final RefreshTokenService refService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final OrganizationService organizationService;

    public AuthController(RefreshTokenService refService, JwtUtil jwtUtil, UserService userService, OrganizationService organizationService) {
        this.jwtUtil = jwtUtil;
        this.refService = refService;
        this.userService = userService;
        this.organizationService = organizationService;
    }


    /*MENTION IN JAVADOC, POST and not GET due to REST state idempotency*/

    // TODO: Redundant DB hit — getUserByRefreshToken and rotateRefreshToken both call
    // validateRefreshToken internally. Refactor using RotationResult record to eliminate
    // duplicate hash + DB lookup in the refresh flow. See RefreshTokenService.rotateRefreshToken.
    @PostMapping("/refresh")
    public ResponseEntity<?> grantRefreshToken(@CookieValue("refresh_token") String cookie) {
        logger.info("Refresh token request received");

        RefreshToken storedToken = refService.getStoredRefreshToken(cookie);
        User user = storedToken.getUser();
        OrgUserBridge bridge = organizationService.resolveRefreshMembership(
            user.getId(),
            storedToken.getOrganization() != null ? storedToken.getOrganization().getId() : null
        );
        ResponseCookie refToken = refService.rotateRefreshToken(storedToken, bridge.getOrganization());
        String jwtToken = jwtUtil.generateToken(user, bridge);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refToken.toString()).body(jwtToken);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logoutUser(@CookieValue(value = "refresh_token", required = false) String cookie) {
        logger.info("Logout request received");
        if (cookie != null) {
            try {
                User user = refService.getUserByRefreshToken(cookie);
                userService.logout(user);
            } catch (Exception error) {
                logger.warn("Logout token lookup failed, clearing cookie anyway: {}", error.getMessage());
            }
        }

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

        User user = userService.login(request.email(), request.password());
        OrgUserBridge bridge = organizationService.findDefaultMembershipForUser(user.getId()).orElse(null);
        String jwtToken = jwtUtil.generateToken(user, bridge);

        ResponseCookie refToken = refService.createAndStoreRefreshToken(
            user,
            bridge != null ? bridge.getOrganization() : null
        );

        logger.info("Login successful for user: {}", request.email());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refToken.toString()).body(jwtToken);
    }

    @PostMapping("/switch-organization")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> switchOrganization(
            @Valid @RequestBody SwitchOrganizationRequest request,
            @CookieValue("refresh_token") String cookie,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        RefreshToken storedToken = refService.getStoredRefreshTokenForUser(cookie, principal.userId());
        OrgUserBridge bridge = organizationService.getMembershipForUser(principal.userId(), request.organizationId());
        String jwtToken = jwtUtil.generateToken(storedToken.getUser(), bridge);
        ResponseCookie refToken = refService.rotateRefreshToken(storedToken, bridge.getOrganization());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refToken.toString())
                .body(jwtToken);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordData(request.password());
        user.setLegalName(request.legalName());
        userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }
}
