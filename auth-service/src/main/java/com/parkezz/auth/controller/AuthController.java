package com.parkezz.auth.controller;

import com.parkezz.auth.dto.LoginRequest;
import com.parkezz.auth.dto.LoginResponse;
import com.parkezz.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and JWT issuance")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with email and password, receive a JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Small protected endpoint that exists purely to demonstrate the JwtFilter
     * actually authenticating a request within Auth Service itself.
     */
    @GetMapping("/me")
    @Operation(summary = "Return the identity of the currently authenticated user (requires JWT)")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "email", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
    }
}
