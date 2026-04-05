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
import com.grimni.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

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

    private User createUser(String username, String email, String passwordHash) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
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

            when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("plaintext")).thenReturn("$2a$hashed");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User saved = userService.register(user);

            assertNotNull(saved);
            assertEquals("alice", saved.getUsername());
            verify(userRepository).save(user);
        }

        @Test
        void register_passwordIsHashedBeforeSaving() {
            User user = createUser("bob", "bob@test.com", "mypassword");

            when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("mypassword")).thenReturn("$2a$hashed");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User saved = userService.register(user);

            assertEquals("$2a$hashed", saved.getPasswordHash());
            assertNotEquals("mypassword", saved.getPasswordHash());
            verify(passwordEncoder).encode("mypassword");
        }

        @Test
        void register_failsWhenUsernameAlreadyExists() {
            User user = createUser("alice", "alice@test.com", "pass");
            when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new User()));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.register(user));

            assertEquals("Username already exists", ex.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        void register_failsWhenEmailAlreadyExists() {
            User user = createUser("charlie", "taken@test.com", "pass");
            when(userRepository.findByUsername("charlie")).thenReturn(Optional.empty());
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
            when(userRepository.findByUsername("alice")).thenReturn(Optional.of(stored));
            when(passwordEncoder.matches("correctpass", "$2a$hashed")).thenReturn(true);

            User result = userService.login("alice", "correctpass");

            assertNotNull(result);
            assertEquals("alice", result.getUsername());
        }

        @Test
        void login_failsWhenUsernameNotFound() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.login("ghost", "pass"));

            assertEquals("User not found", ex.getMessage());
        }

        @Test
        void login_failsWhenPasswordIsIncorrect() {
            User stored = createUser("alice", "alice@test.com", "$2a$hashed");
            when(userRepository.findByUsername("alice")).thenReturn(Optional.of(stored));
            when(passwordEncoder.matches("wrongpass", "$2a$hashed")).thenReturn(false);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.login("alice", "wrongpass"));

            assertEquals("Invalid password", ex.getMessage());
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
            assertEquals("alice", result.getUsername());
            assertEquals(1L, result.getId());
        }

        @Test
        void findUserById_failsWhenIdDoesNotExist() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.findUserById(999L));

            assertEquals("User not found", ex.getMessage());
        }
    }
}
