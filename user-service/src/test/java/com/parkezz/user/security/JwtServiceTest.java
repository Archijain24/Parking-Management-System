package com.parkezz.user.security;

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
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiryOffsetMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void extractUserId_and_extractRole_returnValuesEncodedInToken() {
        String token = buildToken(7L, "OWNER", 60_000);

        assertThat(jwtService.extractUserId(token)).isEqualTo(7L);
        assertThat(jwtService.extractRole(token)).isEqualTo("OWNER");
    }

    @Test
    void validateToken_freshToken_returnsTrue() {
        String token = buildToken(1L, "CUSTOMER", 60_000);
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        String token = buildToken(1L, "CUSTOMER", -60_000);
        assertThat(jwtService.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtService.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void validateToken_tokenSignedWithDifferentKey_returnsFalse() {
        Key otherKey = Keys.hmacShaKeyFor("a-completely-different-32-byte-secret-key!!".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject("someone")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey, SignatureAlgorithm.HS256)
                .compact();

        assertThat(jwtService.validateToken(token)).isFalse();
    }
}
