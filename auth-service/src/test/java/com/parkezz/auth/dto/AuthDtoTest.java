package com.parkezz.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AuthDtoTest {

    @Test
    void loginRequest_noArgsConstructorAndSetters_workCorrectly() {
        LoginRequest request = new LoginRequest();

        request.setEmail("john@example.com");
        request.setPassword("password123");

        assertThat(request.getEmail()).isEqualTo("john@example.com");
        assertThat(request.getPassword()).isEqualTo("password123");
    }

    @Test
    void loginRequest_allArgsConstructor_worksCorrectly() {
        LoginRequest request =
                new LoginRequest("john@example.com", "password123");

        assertThat(request.getEmail()).isEqualTo("john@example.com");
        assertThat(request.getPassword()).isEqualTo("password123");
    }

    @Test
    void loginRequest_equalsAndHashCode_workCorrectly() {
        LoginRequest first =
                new LoginRequest("john@example.com", "password123");

        LoginRequest second =
                new LoginRequest("john@example.com", "password123");

        LoginRequest different =
                new LoginRequest("other@example.com", "different");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(different);
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo("some string");
    }

    @Test
    void loginRequest_toString_containsFieldValues() {
        LoginRequest request =
                new LoginRequest("john@example.com", "password123");

        String result = request.toString();

        assertThat(result).contains("john@example.com");
        assertThat(result).contains("password123");
    }

    @Test
    void loginResponse_noArgsConstructorAndSetters_workCorrectly() {
        LoginResponse response = new LoginResponse();

        response.setToken("access-token");
        response.setUserId(1L);
        response.setName("John");
        response.setEmail("john@example.com");
        response.setRole("ROLE_DRIVER");
        response.setTokenType("Bearer");

        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo("ROLE_DRIVER");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void loginResponse_allArgsConstructor_worksCorrectly() {
        LoginResponse response =
                new LoginResponse(
                        "access-token",
                        1L,
                        "John",
                        "john@example.com",
                        "ROLE_DRIVER",
                        "Bearer");

        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo("ROLE_DRIVER");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void loginResponse_builder_worksCorrectly() {
        LoginResponse response = LoginResponse.builder()
                .token("access-token")
                .userId(1L)
                .name("John")
                .email("john@example.com")
                .role("ROLE_DRIVER")
                .tokenType("Bearer")
                .build();

        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo("ROLE_DRIVER");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void loginResponse_equalsAndHashCode_workCorrectly() {
        LoginResponse first = LoginResponse.builder()
                .token("token")
                .userId(1L)
                .name("John")
                .email("john@example.com")
                .role("ROLE_DRIVER")
                .tokenType("Bearer")
                .build();

        LoginResponse second = LoginResponse.builder()
                .token("token")
                .userId(1L)
                .name("John")
                .email("john@example.com")
                .role("ROLE_DRIVER")
                .tokenType("Bearer")
                .build();

        LoginResponse different = LoginResponse.builder()
                .token("different")
                .userId(2L)
                .name("Jane")
                .email("jane@example.com")
                .role("ROLE_OWNER")
                .tokenType("Bearer")
                .build();

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(different);
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo("some string");
    }

    @Test
    void loginResponse_toString_containsFieldValues() {
        LoginResponse response = new LoginResponse(
                "token",
                1L,
                "John",
                "john@example.com",
                "ROLE_DRIVER",
                "Bearer");

        String result = response.toString();

        assertThat(result).contains("token");
        assertThat(result).contains("John");
        assertThat(result).contains("john@example.com");
        assertThat(result).contains("ROLE_DRIVER");
        assertThat(result).contains("Bearer");
    }

    @Test
    void userInternalResponse_noArgsConstructorAndSetters_workCorrectly() {
        UserInternalResponse response = new UserInternalResponse();

        response.setUserId(1L);
        response.setName("John");
        response.setEmail("john@example.com");
        response.setPassword("hashed-password");
        response.setRole("ROLE_DRIVER");

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getPassword()).isEqualTo("hashed-password");
        assertThat(response.getRole()).isEqualTo("ROLE_DRIVER");
    }

    @Test
    void userInternalResponse_allArgsConstructor_worksCorrectly() {
        UserInternalResponse response =
                new UserInternalResponse(
                        1L,
                        "John",
                        "john@example.com",
                        "hashed-password",
                        "ROLE_DRIVER");

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getPassword()).isEqualTo("hashed-password");
        assertThat(response.getRole()).isEqualTo("ROLE_DRIVER");
    }

    @Test
    void userInternalResponse_equalsAndHashCode_workCorrectly() {
        UserInternalResponse first =
                new UserInternalResponse(
                        1L,
                        "John",
                        "john@example.com",
                        "hashed-password",
                        "ROLE_DRIVER");

        UserInternalResponse second =
                new UserInternalResponse(
                        1L,
                        "John",
                        "john@example.com",
                        "hashed-password",
                        "ROLE_DRIVER");

        UserInternalResponse different =
                new UserInternalResponse(
                        2L,
                        "Jane",
                        "jane@example.com",
                        "other-password",
                        "ROLE_OWNER");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(different);
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo("some string");
    }

    @Test
    void userInternalResponse_toString_containsFieldValues() {
        UserInternalResponse response =
                new UserInternalResponse(
                        1L,
                        "John",
                        "john@example.com",
                        "hashed-password",
                        "ROLE_DRIVER");

        String result = response.toString();

        assertThat(result).contains("John");
        assertThat(result).contains("john@example.com");
        assertThat(result).contains("ROLE_DRIVER");
    }

    @Test
    void errorResponse_noArgsConstructorAndSetters_workCorrectly() {
        ErrorResponse response = new ErrorResponse();
        LocalDateTime timestamp = LocalDateTime.now();

        response.setTimestamp(timestamp);
        response.setStatus(401);
        response.setMessage("Invalid email or password");
        response.setPath("/api/auth/login");

        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getMessage())
                .isEqualTo("Invalid email or password");
        assertThat(response.getPath())
                .isEqualTo("/api/auth/login");
    }

    @Test
    void errorResponse_allArgsConstructor_worksCorrectly() {
        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse response =
                new ErrorResponse(
                        timestamp,
                        404,
                        "User not found",
                        "/api/auth/me");

        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("User not found");
        assertThat(response.getPath()).isEqualTo("/api/auth/me");
    }

    @Test
    void errorResponse_builder_worksCorrectly() {
        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .message("Bad request")
                .path("/api/auth/login")
                .build();

        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo("Bad request");
        assertThat(response.getPath()).isEqualTo("/api/auth/login");
    }

    @Test
    void errorResponse_equalsAndHashCode_workCorrectly() {
        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse first =
                new ErrorResponse(
                        timestamp,
                        400,
                        "Bad request",
                        "/api/auth/login");

        ErrorResponse second =
                new ErrorResponse(
                        timestamp,
                        400,
                        "Bad request",
                        "/api/auth/login");

        ErrorResponse different =
                new ErrorResponse(
                        timestamp,
                        404,
                        "Not found",
                        "/api/auth/me");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(different);
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo("some string");
    }

    @Test
    void errorResponse_toString_containsFieldValues() {
        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse response =
                new ErrorResponse(
                        timestamp,
                        400,
                        "Bad request",
                        "/api/auth/login");

        String result = response.toString();

        assertThat(result).contains("400");
        assertThat(result).contains("Bad request");
        assertThat(result).contains("/api/auth/login");
    }
}