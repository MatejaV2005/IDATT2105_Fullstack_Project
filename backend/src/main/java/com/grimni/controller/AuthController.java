package com.grimni.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
    /*MENTION IN JAVADOC, POST and not GET due to REST state idempotency*/ 

    // TODO: Redundant DB hit — getUserByRefreshToken and rotateRefreshToken both call
    // validateRefreshToken internally. Refactor using RotationResult record to eliminate
    // duplicate hash + DB lookup in the refresh flow. See RefreshTokenService.rotateRefreshToken.
    @PostMapping("/refresh")
    public ResponseEntity<?> grantRefreshToken(@CookieValue("refresh_token") String cookie) {
        try {
            logger.info("Refresh token request received");

            User user = refService.getUserByRefreshToken(cookie);
            ResponseCookie refToken = refService.rotateRefreshToken(user, cookie);
            String jwtToken = jwtUtil.generateToken(user);

            // return status 200 OK + set the rotated refresh token in header and new JWT token in body
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refToken.toString()).body(jwtToken);
                            
            } catch(Exception error) {
                return ResponseEntity.status(401).body(error.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        logger.info("Login attempt for user: {}", request.username());
        try {
            User user = userService.login(request.username(), request.password());
            String jwtToken = jwtUtil.generateToken(user);

            ResponseCookie refToken = refService.createAndStoreRefreshToken(user);

            logger.info("Login successful for user: {}", request.username());
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refToken.toString()).body(jwtToken);

        } catch(Exception error) {
            logger.warn("Login failed for user '{}': {}", request.username(), error.getMessage());
            return ResponseEntity.status(401).body(error.getMessage());
        }
    } 
}
