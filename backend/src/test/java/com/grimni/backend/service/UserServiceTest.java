package com.grimni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.User;
import com.grimni.repository.UserRepository;
import com.grimni.service.RefreshTokenService;
import com.grimni.service.UserService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // BCryptPasswordEncoder is created via new inside UserService constructor,
        // so @InjectMocks won't inject the mock — replace it manually
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
    }

    private User createUser(String legalName, String email, String passwordHash) {
        User user = new User();
        user.setLegalName(legalName);
        user.setEmail(email);
        user.setPasswordData(passwordHash);
        return user;
    }

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------
    @Nested
    class RegisterTests {

        @Test
        void register_success_returnsSavedUser() {
            User user = createUser("alice", "alice@test.com", "plaintext");

            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("plaintext")).thenReturn("$2a$hashed");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User saved = userService.register(user);

            assertNotNull(saved);
            assertEquals("alice", saved.getLegalName());
            verify(userRepository).save(user);
        }

        @Test
        void register_passwordIsHashedBeforeSaving() {
            User user = createUser("bob", "bob@test.com", "mypassword");

            when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("mypassword")).thenReturn("$2a$hashed");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User saved = userService.register(user);

            assertEquals("$2a$hashed", saved.getPasswordData());
            assertNotEquals("mypassword", saved.getPasswordData());
            verify(passwordEncoder).encode("mypassword");
        }

        @Test
        void register_failsWhenEmailAlreadyExists() {
            User user = createUser("charlie", "taken@test.com", "pass");
            when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(new User()));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.register(user));

            assertEquals("Email already exists", ex.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------
    @Nested
    class LoginTests {

        @Test
        void login_success_returnsUser() {
            User stored = createUser("alice", "alice@test.com", "$2a$hashed");
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(stored));
            when(passwordEncoder.matches("correctpass", "$2a$hashed")).thenReturn(true);

            User result = userService.login("alice@test.com", "correctpass");

            assertNotNull(result);
            assertEquals("alice", result.getLegalName());
        }

        @Test
        void login_failsWhenEmailNotFound() {
            when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

            BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                    () -> userService.login("ghost@test.com", "pass"));

            assertEquals("Invalid email or password", ex.getMessage());
        }

        @Test
        void login_failsWhenPasswordIsIncorrect() {
            User stored = createUser("alice", "alice@test.com", "$2a$hashed");
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(stored));
            when(passwordEncoder.matches("wrongpass", "$2a$hashed")).thenReturn(false);

            BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                    () -> userService.login("alice@test.com", "wrongpass"));

            assertEquals("Invalid email or password", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Logout
    // -------------------------------------------------------------------------
    @Nested
    class LogoutTests {

        @Test
        void logout_success_revokesAllTokens() {
            User user = createUser("alice", "alice@test.com", "pass");

            userService.logout(user);

            verify(refreshTokenService).revokeAllTokens(user);
        }

        @Test
        void logout_failsWhenUserIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.logout(null));

            assertEquals("User cannot be null", ex.getMessage());
            verify(refreshTokenService, never()).revokeAllTokens(any());
        }
    }

    // -------------------------------------------------------------------------
    // FindUserById
    // -------------------------------------------------------------------------
    @Nested
    class FindUserByIdTests {

        @Test
        void findUserById_success_returnsUser() {
            User user = createUser("alice", "alice@test.com", "pass");
            ReflectionTestUtils.setField(user, "id", 1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            User result = userService.findUserById(1L);

            assertNotNull(result);
            assertEquals("alice", result.getLegalName());
            assertEquals(1L, result.getId());
        }

        @Test
        void findUserById_failsWhenIdDoesNotExist() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> userService.findUserById(999L));

            assertEquals("User not found", ex.getMessage());
        }
    }
}
