package com.grimni.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.grimni.domain.User;
import com.grimni.dto.UpdateUserRequest;
import com.grimni.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.authentication.BadCredentialsException;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User register(User user) {
        logger.info("Attempting to register user: {}", user.getEmail());

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            logger.warn("Registration failed: email '{}' already exists", user.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        // Bcrypt automatically generates salt for password, stores salt together in the same string with the hashed password+salt
        user.setPasswordData(passwordEncoder.encode(user.getPasswordData())); 
        User saved = userRepository.save(user);
        logger.info("User '{}' registered successfully", saved.getLegalName());  
        return saved;
    }

    public User login(String email, String password) {
        logger.info("Login attempt for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Login failed: user '{}' not found", email);
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(password, user.getPasswordData())) {
            logger.warn("Login failed: invalid password for user '{}'", email);
            throw new BadCredentialsException("Invalid email or password");
        }

        logger.info("User '{}' logged in successfully", email);
        return user;
    }

    public void logout(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        logger.info("Logging out user: {}", user.getLegalName()); 
        refreshTokenService.revokeAllTokens(user);
        logger.info("User '{}' logged out successfully", user.getLegalName()); 
    }

    public User findUserById(Long id) {
        logger.info("Looking up user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new EntityNotFoundException("User not found");
                });
    }

    public User updateUser(Long id, UpdateUserRequest request) {
        logger.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Update failed: user {} not found", id);
                    return new EntityNotFoundException("User not found");
                });

        if (request.legalName() != null) user.setLegalName(request.legalName());
        if (request.email() != null) user.setEmail(request.email());

        User saved = userRepository.save(user);
        logger.info("User {} updated successfully", id);
        return saved;
    }
}
