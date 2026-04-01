package com.grimni.backend.token;

import com.grimni.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    // Valid base64 secret that decodes to >= 32 bytes (required for HMAC-SHA256)
    // Decodes to: "this is a test secret key for unit testing" (43 bytes)
    private static final String TEST_SECRET =
            "dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGtleSBmb3IgdW5pdCB0ZXN0aW5n";

    // -------------------------------------------------------------------------
    // Spring context slice — verifies that jwt.secret is loaded from properties
    // -------------------------------------------------------------------------
    @Nested
    @ExtendWith(SpringExtension.class)
    @Import(JwtUtil.class)
    @TestPropertySource(properties = "jwt.secret=dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGtleSBmb3IgdW5pdCB0ZXN0aW5n")
    class PropertyLoadingTest {

        @Autowired
        JwtUtil jwtUtil;

        @Value("${jwt.secret}")
        String injectedSecret;

        @Test
        void secretIsInjectedFromProperties() {
            assertNotNull(injectedSecret, "jwt.secret should be injected from properties");
            assertFalse(injectedSecret.isBlank(), "jwt.secret should not be blank");
        }

        @Test
        void jwtUtilBeanInitialisesWithInjectedSecret() {
            // If the key was invalid, @PostConstruct init() would have thrown —
            // being able to call generateToken proves the key loaded and parsed correctly
            assertNotNull(jwtUtil);
            String token = jwtUtil.generateToken("testuser");
            assertNotNull(token, "JwtUtil bean should be fully functional with the injected secret");
        }
    }

    // -------------------------------------------------------------------------
    // Unit tests — no Spring context, secret injected via ReflectionTestUtils
    // -------------------------------------------------------------------------
    @Nested
    class UnitTests {

        private JwtUtil jwtUtil;

        @BeforeEach
        void setUp() {
            jwtUtil = new JwtUtil();
            ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
            ReflectionTestUtils.invokeMethod(jwtUtil, "init"); // triggers @PostConstruct manually
        }

        // --- generateToken ---------------------------------------------------

        @Test
        void generateToken_returnsNonNullToken() {
            assertNotNull(jwtUtil.generateToken("alice"));
        }

        @Test
        void generateToken_tokenHasThreeParts() {
            // JWT structure: header.payload.signature
            String token = jwtUtil.generateToken("alice");
            assertEquals(3, token.split("\\.").length);
        }

        @Test
        void generateToken_differentUsernames_produceDifferentTokens() {
            String token1 = jwtUtil.generateToken("alice");
            String token2 = jwtUtil.generateToken("bob");
            assertNotEquals(token1, token2);
        }

        // --- isTokenValid ----------------------------------------------------

        @Test
        void isTokenValid_freshToken_returnsTrue() {
            String token = jwtUtil.generateToken("alice");
            assertTrue(jwtUtil.isTokenValid(token));
        }

        @Test
        void isTokenValid_tamperedSignature_returnsFalse() {
            String token = jwtUtil.generateToken("alice");
            String tampered = token.substring(0, token.length() - 4) + "xxxx";
            assertFalse(jwtUtil.isTokenValid(tampered));
        }

        @Test
        void isTokenValid_tokenSignedWithDifferentKey_returnsFalse() {
            // Build a token signed by a different key
            JwtUtil otherUtil = new JwtUtil();
            // Decodes to: "anotherTestSecretKeyForUnitTestingOnly123" (41 bytes)
            ReflectionTestUtils.setField(otherUtil, "secret",
                    "YW5vdGhlclRlc3RTZWNyZXRLZXlGb3JVbml0VGVzdGluZ09ubHkxMjM=");
            ReflectionTestUtils.invokeMethod(otherUtil, "init");

            String foreignToken = otherUtil.generateToken("alice");
            assertFalse(jwtUtil.isTokenValid(foreignToken));
        }

        @Test
        void isTokenValid_randomString_returnsFalse() {
            assertFalse(jwtUtil.isTokenValid("not.a.jwt.token"));
        }

        @Test
        void isTokenValid_emptyString_returnsFalse() {
            assertFalse(jwtUtil.isTokenValid(""));
        }

        // --- extractUsername -------------------------------------------------

        @Test
        void extractUsername_validToken_returnsCorrectUsername() {
            String token = jwtUtil.generateToken("alice");
            assertEquals("alice", jwtUtil.extractUsername(token));
        }

        @Test
        void extractUsername_validToken_doesNotReturnWrongUsername() {
            String token = jwtUtil.generateToken("alice");
            assertNotEquals("bob", jwtUtil.extractUsername(token));
        }

        @Test
        void extractUsername_invalidToken_returnsNull() {
            assertNull(jwtUtil.extractUsername("invalid.token.string"));
        }

        @Test
        void extractUsername_tokenSignedWithDifferentKey_returnsNull() {
            JwtUtil otherUtil = new JwtUtil();
            ReflectionTestUtils.setField(otherUtil, "secret",
                    "YW5vdGhlclRlc3RTZWNyZXRLZXlGb3JVbml0VGVzdGluZ09ubHkxMjM=");
            ReflectionTestUtils.invokeMethod(otherUtil, "init");

            String foreignToken = otherUtil.generateToken("alice");
            assertNull(jwtUtil.extractUsername(foreignToken));
        }
    }
}
