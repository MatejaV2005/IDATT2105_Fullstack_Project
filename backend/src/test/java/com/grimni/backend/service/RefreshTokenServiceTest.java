package com.grimni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import com.grimni.domain.RefreshToken;
import com.grimni.domain.User;
import com.grimni.repository.RefreshTokenRepository;
import com.grimni.service.RefreshTokenService;
import com.grimni.util.RefreshTokenUtil;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenUtil util;

    @Mock
    private RefreshTokenRepository repository;

    @InjectMocks
    private RefreshTokenService service;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setLegalName("alice"); // ? Wallah
        // User.id is DB-assigned (GenerationType.IDENTITY), no setter — use reflection
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    // -------------------------------------------------------------------------
    // rotateRefreshToken
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("rotateRefreshToken")
    class RotateRefreshTokenTests {

        @Test
        @DisplayName("hashes incoming token, deletes existing, and returns new cookie")
        void rotateRefreshToken_success() {
            String incoming = "plaintext-token";
            String hashed = "hashed-token";
            RefreshToken existing = new RefreshToken();
            existing.setUser(testUser);
            existing.setRefreshToken(hashed);

            ResponseCookie expectedCookie = ResponseCookie.from("refresh_token", "new-plaintext").build();

            when(util.hashToken(incoming)).thenReturn(hashed);
            when(repository.findByRefreshToken(hashed)).thenReturn(Optional.of(existing));
            when(util.generateRefreshToken()).thenReturn("new-plaintext");
            when(util.hashToken("new-plaintext")).thenReturn("new-hashed");
            when(repository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
            when(util.createRefreshTokenCookie("new-plaintext")).thenReturn(expectedCookie);

            ResponseCookie result = service.rotateRefreshToken(testUser, incoming);

            assertNotNull(result);
            verify(util).hashToken(incoming);
            verify(repository).findByRefreshToken(hashed);
            verify(repository).delete(existing);
            verify(repository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("throws BadCredentialsException when token does not exist in database")
        void rotateRefreshToken_tokenNotFound_throws() {
            when(util.hashToken("unknown-token")).thenReturn("hashed-unknown");
            when(repository.findByRefreshToken("hashed-unknown")).thenReturn(Optional.empty());

            BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                    () -> service.rotateRefreshToken(testUser, "unknown-token"));

            assertEquals("Invalid refresh token", ex.getMessage());
            verify(repository, never()).delete(any());
            verify(repository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // revokeAllTokens
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("revokeAllTokens")
    class RevokeAllTokensTests {

        @Test
        @DisplayName("calls deleteByUser on repository for the correct user")
        void revokeAllTokens_callsDeleteByUser() {
            service.revokeAllTokens(testUser);

            verify(repository).deleteByUser(testUser);
        }
    }

    // -------------------------------------------------------------------------
    // createAndStoreRefreshToken
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createAndStoreRefreshToken")
    class CreateAndStoreTests {

        @Test
        @DisplayName("generates token, hashes it, saves entity with correct fields, returns cookie with plaintext")
        void createAndStore_success() {
            String plaintext = "generated-plaintext";
            String hashed = "generated-hashed";
            ResponseCookie expectedCookie = ResponseCookie.from("refresh_token", plaintext).build();

            when(util.generateRefreshToken()).thenReturn(plaintext);
            when(util.hashToken(plaintext)).thenReturn(hashed);
            when(repository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
            when(util.createRefreshTokenCookie(plaintext)).thenReturn(expectedCookie);

            ResponseCookie result = service.createAndStoreRefreshToken(testUser);

            assertEquals(expectedCookie, result);

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(repository).save(captor.capture());
            RefreshToken saved = captor.getValue();

            assertEquals(testUser, saved.getUser());
            assertEquals(hashed, saved.getRefreshToken());
            // createdAt is populated by the DB (@Column insertable=false), not by JPA in-memory.
            // In a unit test with no persistence, it will be null — assertion removed.
            verify(util).createRefreshTokenCookie(plaintext);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when user is null")
        void createAndStore_nullUser_throws() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.createAndStoreRefreshToken(null));

            assertEquals("User cannot be null", ex.getMessage());
            verify(repository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // getUserByRefreshToken
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getUserByRefreshToken")
    class GetUserByRefreshTokenTests {

        @Test
        @DisplayName("hashes incoming token, finds entity, and returns the correct user")
        void getUserByRefreshToken_success() {
            String incoming = "plaintext-token";
            String hashed = "hashed-token";
            RefreshToken entity = new RefreshToken();
            entity.setUser(testUser);
            entity.setRefreshToken(hashed);

            when(util.hashToken(incoming)).thenReturn(hashed);
            when(repository.findByRefreshToken(hashed)).thenReturn(Optional.of(entity));

            User result = service.getUserByRefreshToken(incoming);

            assertEquals(testUser, result);
            verify(util).hashToken(incoming);
            verify(repository).findByRefreshToken(hashed);
        }

        @Test
        @DisplayName("throws BadCredentialsException when token does not exist in database")
        void getUserByRefreshToken_tokenNotFound_throws() {
            when(util.hashToken("missing-token")).thenReturn("hashed-missing");
            when(repository.findByRefreshToken("hashed-missing")).thenReturn(Optional.empty());

            BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                    () -> service.getUserByRefreshToken("missing-token"));

            assertEquals("Invalid refresh token", ex.getMessage());
        }
    }
}
