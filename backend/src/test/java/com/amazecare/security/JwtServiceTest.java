package com.amazecare.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Provide deterministic secret and expiration via reflection to avoid needing Spring context
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "this_is_a_test_secret_key_that_is_long_enough_123456");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 60_000L);
    }

    @Test
    void generate_and_validate_token() {
        String token = jwtService.generateToken("alice", Map.of("role", "ADMIN"));
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("alice", username);

        assertTrue(jwtService.isTokenValid(token, "alice"));
        assertFalse(jwtService.isTokenValid(token, "bob"));
    }
}


