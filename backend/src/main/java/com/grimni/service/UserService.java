package com.grimni.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.grimni.domain.User;
import com.grimni.repository.UserRepository;

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
        logger.info("Attempting to register user: {}", user.getLegalName()); // ? Wallah
        if (userRepository.findByLegalName(user.getLegalName()).isPresent()) { // ? Wallah
            logger.warn("Registration failed: username '{}' already exists", user.getLegalName()); // ? Wallah
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            logger.warn("Registration failed: email '{}' already exists", user.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        // Bcrypt automatically generates salt for password, stores salt together in the same string with the hashed password+salt
        user.setPasswordData(passwordEncoder.encode(user.getPasswordData())); // ? Wallah
        User saved = userRepository.save(user);
        logger.info("User '{}' registered successfully", saved.getLegalName());  // ? Wallah
        return saved;
    }

    public User login(String username, String password) {
        logger.info("Login attempt for user: {}", username);
        User user = userRepository.findByLegalName(username)
                .orElseThrow(() -> {
                    logger.warn("Login failed: user '{}' not found", username);
                    return new IllegalArgumentException("User not found");
                });

        if (!passwordEncoder.matches(password, user.getPasswordData())) {  // ? Wallah
            logger.warn("Login failed: invalid password for user '{}'", username);
            throw new IllegalArgumentException("Invalid password");
        }

        logger.info("User '{}' logged in successfully", username);
        return user;
    }

    public void logout(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        logger.info("Logging out user: {}", user.getLegalName()); // ? Wallah
        refreshTokenService.revokeAllTokens(user);
        logger.info("User '{}' logged out successfully", user.getLegalName()); // ? Wallah
    }

    public User findUserById(Long id) {
        logger.info("Looking up user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new IllegalArgumentException("User not found");
                });
    }
}
