package com.cms.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-must-be-at-least-32-characters-long");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_ShouldReturnNonNullToken() {
        String token = jwtUtil.generateToken("ibrahim@test.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_ShouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken("ibrahim@test.com");
        String username = jwtUtil.extractEmail(token);
        assertEquals("ibrahim@test.com", username);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForValidToken() {
        String token = jwtUtil.generateToken("ibrahim@test.com");
        assertTrue(jwtUtil.isTokenValid(token, "ibrahim@test.com"));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForWrongUsername() {
        String token = jwtUtil.generateToken("ibrahim@test.com");
        assertFalse(jwtUtil.isTokenValid(token, "other@test.com"));
    }

    @Test
    void generateToken_ShouldGenerateDifferentTokens_ForDifferentUsers() {
        String token1 = jwtUtil.generateToken("user1@test.com");
        String token2 = jwtUtil.generateToken("user2@test.com");
        assertNotEquals(token1, token2);
    }

    @Test
    void extractEmail_ShouldWorkWithPhoneNumber() {
        String token = jwtUtil.generateToken("03001234567");
        String username = jwtUtil.extractEmail(token);
        assertEquals("03001234567", username);
    }
}