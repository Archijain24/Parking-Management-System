package com.parkezz.auth.service;

import com.parkezz.auth.client.UserServiceClient;
import com.parkezz.auth.dto.LoginRequest;
import com.parkezz.auth.dto.LoginResponse;
import com.parkezz.auth.dto.UserInternalResponse;
import com.parkezz.auth.exception.InvalidCredentialsException;
import com.parkezz.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest loginRequest;
    private UserInternalResponse userInternalResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("john@example.com", "password123");
        userInternalResponse = new UserInternalResponse(
                1L, "John Doe", "john@example.com", "hashed-password", "CUSTOMER");
    }

    @Test
    void login_validCredentials_returnsTokenAndUserInfo() {
        when(userServiceClient.getUserByEmail("john@example.com")).thenReturn(userInternalResponse);
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken("john@example.com", 1L, "CUSTOMER")).thenReturn("dummy.jwt.token");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("dummy.jwt.token");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        when(userServiceClient.getUserByEmail("john@example.com")).thenReturn(userInternalResponse);
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_userNotFound_throwsInvalidCredentialsException() {
        when(userServiceClient.getUserByEmail("john@example.com"))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_userServiceReturnsNull_throwsInvalidCredentialsException() {
        when(userServiceClient.getUserByEmail("john@example.com")).thenReturn(null);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_ownerRole_returnsOwnerInLoginResponse() {
        UserInternalResponse owner = new UserInternalResponse(2L, "Jane Owner", "jane@example.com", "hashed-pw", "OWNER");
        LoginRequest ownerLogin = new LoginRequest("jane@example.com", "ownerpass");
        when(userServiceClient.getUserByEmail("jane@example.com")).thenReturn(owner);
        when(passwordEncoder.matches("ownerpass", "hashed-pw")).thenReturn(true);
        when(jwtService.generateToken("jane@example.com", 2L, "OWNER")).thenReturn("owner.jwt.token");

        LoginResponse response = authService.login(ownerLogin);

        assertThat(response.getRole()).isEqualTo("OWNER");
        assertThat(response.getUserId()).isEqualTo(2L);
    }
}
