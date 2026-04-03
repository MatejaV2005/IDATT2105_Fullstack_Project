package com.grimni.backend.token;

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

import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.User.Role;
import com.grimni.util.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    // Valid base64 secret that decodes to >= 32 bytes (required for HMAC-SHA256)
    // Decodes to: "this is a test secret key for unit testing" (43 bytes)
    private static final String TEST_SECRET =
            "dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGtleSBmb3IgdW5pdCB0ZXN0aW5n";

    /**
     * Builds a User entity for testing via ReflectionTestUtils.
     * Since User is a JPA entity with no setters, we use reflection to set
     * private fields directly — this is the standard approach for testing
     * entities without exposing setters in the domain model.
     */
    private static User createTestUser(String username, Long userId, Role role, Long orgId) {
        Organization org = new Organization();
        org.setId(orgId);

        User user = new User();
        ReflectionTestUtils.setField(user, "username", username);
        ReflectionTestUtils.setField(user, "userId", userId);
        ReflectionTestUtils.setField(user, "role", role);
        ReflectionTestUtils.setField(user, "organization", org);
        return user;
    }

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
            assertNotNull(jwtUtil);
            User user = createTestUser("testuser", 1L, Role.EMPLOYEE, 10L);
            String token = jwtUtil.generateToken(user);
            assertNotNull(token, "JwtUtil bean should be fully functional with the injected secret");
        }
    }

    // -------------------------------------------------------------------------
    // Unit tests — no Spring context, secret injected via ReflectionTestUtils
    // -------------------------------------------------------------------------
    @Nested
    class UnitTests {

        private JwtUtil jwtUtil;
        private User testUser;

        @BeforeEach
        void setUp() {
            jwtUtil = new JwtUtil();
            ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
            ReflectionTestUtils.invokeMethod(jwtUtil, "init");

            testUser = createTestUser("alice", 42L, Role.MANAGER, 7L);
        }

        // --- generateToken ---------------------------------------------------

        @Test
        void generateToken_returnsNonNullToken() {
            assertNotNull(jwtUtil.generateToken(testUser));
        }

        @Test
        void generateToken_tokenHasThreeParts() {
            String token = jwtUtil.generateToken(testUser);
            assertEquals(3, token.split("\\.").length);
        }

        @Test
        void generateToken_differentUsers_produceDifferentTokens() {
            User bob = createTestUser("bob", 99L, Role.EMPLOYEE, 3L);
            String token1 = jwtUtil.generateToken(testUser);
            String token2 = jwtUtil.generateToken(bob);
            assertNotEquals(token1, token2);
        }

        // --- isTokenValid ----------------------------------------------------

        @Test
        void isTokenValid_freshToken_returnsTrue() {
            String token = jwtUtil.generateToken(testUser);
            assertTrue(jwtUtil.isTokenValid(token));
        }

        @Test
        void isTokenValid_tamperedSignature_returnsFalse() {
            String token = jwtUtil.generateToken(testUser);
            String tampered = token.substring(0, token.length() - 4) + "xxxx";
            assertFalse(jwtUtil.isTokenValid(tampered));
        }

        @Test
        void isTokenValid_tokenSignedWithDifferentKey_returnsFalse() {
            JwtUtil otherUtil = new JwtUtil();
            ReflectionTestUtils.setField(otherUtil, "secret",
                    "YW5vdGhlclRlc3RTZWNyZXRLZXlGb3JVbml0VGVzdGluZ09ubHkxMjM=");
            ReflectionTestUtils.invokeMethod(otherUtil, "init");

            String foreignToken = otherUtil.generateToken(testUser);
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
            String token = jwtUtil.generateToken(testUser);
            assertEquals("alice", jwtUtil.extractUsername(token));
        }

        @Test
        void extractUsername_validToken_doesNotReturnWrongUsername() {
            String token = jwtUtil.generateToken(testUser);
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

            String foreignToken = otherUtil.generateToken(testUser);
            assertNull(jwtUtil.extractUsername(foreignToken));
        }

        // --- extractUserId ---------------------------------------------------

        @Test
        void extractUserId_validToken_returnsCorrectId() {
            String token = jwtUtil.generateToken(testUser);
            assertEquals(42L, jwtUtil.extractUserId(token));
        }

        @Test
        void extractUserId_invalidToken_returnsNull() {
            assertNull(jwtUtil.extractUserId("invalid.token.string"));
        }

        // --- extractUserRole -------------------------------------------------

        @Test
        void extractUserRole_validToken_returnsCorrectRole() {
            String token = jwtUtil.generateToken(testUser);
            assertEquals("MANAGER", jwtUtil.extractUserRole(token));
        }

        @Test
        void extractUserRole_employeeRole_returnsEmployee() {
            User employee = createTestUser("bob", 2L, Role.EMPLOYEE, 1L);
            String token = jwtUtil.generateToken(employee);
            assertEquals("EMPLOYEE", jwtUtil.extractUserRole(token));
        }

        @Test
        void extractUserRole_invalidToken_returnsNull() {
            assertNull(jwtUtil.extractUserRole("invalid.token.string"));
        }

        // --- extractUserOrgId ------------------------------------------------

        @Test
        void extractUserOrgId_validToken_returnsCorrectOrgId() {
            String token = jwtUtil.generateToken(testUser);
            assertEquals(7L, jwtUtil.extractUserOrgId(token));
        }

        @Test
        void extractUserOrgId_invalidToken_returnsNull() {
            assertNull(jwtUtil.extractUserOrgId("invalid.token.string"));
        }
    }
}
