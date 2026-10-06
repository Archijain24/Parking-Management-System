package com.parkezz.auth.service;

import com.parkezz.auth.client.UserServiceClient;
import com.parkezz.auth.dto.LoginRequest;
import com.parkezz.auth.dto.LoginResponse;
import com.parkezz.auth.dto.UserInternalResponse;
import com.parkezz.auth.exception.InvalidCredentialsException;
import com.parkezz.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login request received for email={}", request.getEmail());

        UserInternalResponse user;
        try {
            user = userServiceClient.getUserByEmail(request.getEmail());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Invalid login credentials - user not found for email={}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid login credentials for email={}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getUserId(), user.getRole());
        log.info("User authenticated successfully: email={}", request.getEmail());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .tokenType("Bearer")
                .build();
    }
}
