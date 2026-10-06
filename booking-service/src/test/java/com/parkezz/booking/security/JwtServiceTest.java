package com.parkezz.booking.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long-for-hs256";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    private String buildToken(long userId, String role, long expiryOffsetMs) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        Date now = new Date();
        return Jwts.builder()
                .setSubject("user@example.com")
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiryOffsetMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void extractUserId_and_extractRole_returnEncodedValues() {
        String token = buildToken(5L, "CUSTOMER", 60_000);

        assertThat(jwtService.extractUserId(token)).isEqualTo(5L);
        assertThat(jwtService.extractRole(token)).isEqualTo("CUSTOMER");
    }

    @Test
    void validateToken_freshToken_returnsTrue() {
        assertThat(jwtService.validateToken(buildToken(1L, "OWNER", 60_000))).isTrue();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        assertThat(jwtService.validateToken(buildToken(1L, "OWNER", -60_000))).isFalse();
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtService.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void isTokenExpired_freshToken_returnsFalse() {
        assertThat(jwtService.isTokenExpired(buildToken(1L, "OWNER", 60_000))).isFalse();
    }
}
