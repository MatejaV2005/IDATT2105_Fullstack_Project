package com.grimni.backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import com.grimni.util.RefreshTokenUtil;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class RefreshTokenUtilTest {

    private RefreshTokenUtil util;

    @BeforeEach
    void setUp() {
        util = new RefreshTokenUtil();
    }

    // --- generateRefreshToken ------------------------------------------------

    @Nested
    class GenerateRefreshTokenTests {

        @Test
        void generatedToken_isNotNull() {
            assertNotNull(util.generateRefreshToken());
        }

        @Test
        void generatedToken_isNotBlank() {
            assertFalse(util.generateRefreshToken().isBlank());
        }

        @Test
        void generatedToken_decodesTo32Bytes() {
            String token = util.generateRefreshToken();
            byte[] decoded = Base64.getUrlDecoder().decode(token);
            assertEquals(32, decoded.length);
        }

        @Test
        void generatedToken_isValidBase64Url() {
            String token = util.generateRefreshToken();
            assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(token));
        }

        @Test
        void generatedTokens_areUnique() {
            String token1 = util.generateRefreshToken();
            String token2 = util.generateRefreshToken();
            assertNotEquals(token1, token2);
        }
    }

    // --- createRefreshTokenCookie --------------------------------------------

    @Nested
    class CookieTests {

        private ResponseCookie cookie;

        @BeforeEach
        void setUp() {
            cookie = util.createRefreshTokenCookie("test-token-value");
        }

        @Test
        void cookie_hasCorrectName() {
            assertEquals("refresh_token", cookie.getName());
        }

        @Test
        void cookie_hasCorrectValue() {
            assertEquals("test-token-value", cookie.getValue());
        }

        @Test
        void cookie_isHttpOnly() {
            assertTrue(cookie.isHttpOnly());
        }

        @Test
        void cookie_isSecure() {
            assertTrue(cookie.isSecure());
        }

        @Test
        void cookie_hasSameSiteStrict() {
            assertEquals("Strict", cookie.getSameSite());
        }

        @Test
        void cookie_hasCorrectPath() {
            assertEquals("/api/auth", cookie.getPath());
        }

        @Test
        void cookie_hasSevenDayMaxAge() {
            long sevenDaysInSeconds = 7 * 24 * 60 * 60;
            assertEquals(sevenDaysInSeconds, cookie.getMaxAge().getSeconds());
        }
    }

    // --- hashToken -----------------------------------------------------------

    @Nested
    class HashTokenTests {

        @Test
        void hashToken_returnsNonNullResult() {
            assertNotNull(util.hashToken("some-token"));
        }

        @Test
        void hashToken_sameInput_producesSameHash() {
            String hash1 = util.hashToken("my-token");
            String hash2 = util.hashToken("my-token");
            assertEquals(hash1, hash2);
        }

        @Test
        void hashToken_differentInput_producesDifferentHash() {
            String hash1 = util.hashToken("token-a");
            String hash2 = util.hashToken("token-b");
            assertNotEquals(hash1, hash2);
        }

        @Test
        void hashToken_resultIsValidBase64Url() {
            String hash = util.hashToken("some-token");
            assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(hash));
        }

        @Test
        void hashToken_resultDecodesTo32Bytes() {
            String hash = util.hashToken("some-token");
            byte[] decoded = Base64.getUrlDecoder().decode(hash);
            assertEquals(32, decoded.length); // SHA-256 always produces 32 bytes
        }

        @Test
        void hashToken_resultDiffersFromInput() {
            String input = "plaintext-token";
            String hash = util.hashToken(input);
            assertNotEquals(input, hash);
        }
    }
}
