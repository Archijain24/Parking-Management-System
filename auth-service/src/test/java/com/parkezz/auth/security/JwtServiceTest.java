package com.parkezz.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "test-secret-key-that-is-at-least-32-bytes-long-for-hs256");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
    }

    @Test
    void generateToken_thenExtractEmailAndRole_matchOriginalValues() {
        String token = jwtService.generateToken("jane@example.com", 42L, "OWNER");

        assertThat(jwtService.extractEmail(token)).isEqualTo("jane@example.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("OWNER");
    }

    @Test
    void validateToken_freshlyGeneratedToken_returnsTrue() {
        String token = jwtService.generateToken("jane@example.com", 42L, "OWNER");
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String expiredToken = jwtService.generateToken("jane@example.com", 42L, "OWNER");
        assertThat(jwtService.validateToken(expiredToken)).isFalse();
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtService.validateToken("not-a-real-jwt")).isFalse();
    }
}
