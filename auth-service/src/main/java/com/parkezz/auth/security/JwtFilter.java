package com.parkezz.auth.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * A REAL JWT filter that runs once per request, ahead of the controller.
 *
 * Flow:
 * 1. Read the Authorization header.
 * 2. Confirm it starts with "Bearer ".
 * 3. Extract the raw token.
 * 4. Validate its signature and expiry.
 * 5. Pull the email (subject) and role out of the token's claims.
 * 6. Put an authenticated principal into Spring Security's SecurityContext.
 * 7. If anything above fails, simply do not authenticate -> Spring Security
 *    will reject the request with 401 further down the chain for protected
 *    endpoints. Public endpoints (like /api/auth/login) still work because
 *    they don't require authentication in SecurityConfig.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtService.validateToken(token)) {
                    String email = jwtService.extractEmail(token);
                    String role = jwtService.extractRole(token);

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        email, null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } else {
                    log.warn("Rejected request with expired JWT for path {}", request.getRequestURI());
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.warn("Rejected request with invalid JWT for path {}: {}", request.getRequestURI(), e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
