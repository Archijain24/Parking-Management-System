package com.parkezz.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkezz.user.security.JwtService;
import com.parkezz.user.dto.RegisterRequest;
import com.parkezz.user.dto.UserResponse;
import com.parkezz.user.entity.Role;
import com.parkezz.user.exception.UserAlreadyExistsException;
import com.parkezz.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security filters are disabled here because /api/register is a public
 * endpoint (permitAll) - this test focuses purely on request validation and
 * controller wiring, which security tests elsewhere already cover separately.
 */
@WebMvcTest(controllers = RegistrationController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;
    
    @MockBean
    private JwtService jwtService;

    @Test
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123", "9876543210", Role.CUSTOMER);
        UserResponse response = UserResponse.builder()
                .userId(1L).name("John Doe").email("john@example.com").phone("9876543210").role(Role.CUSTOMER).build();
        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void register_invalidEmail_returns400WithMessage() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "hello", "password123", "9876543210", Role.CUSTOMER);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("valid email")));
    }

    @Test
    void register_blankName_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("", "john@example.com", "password123", "9876543210", Role.CUSTOMER);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidPhone_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123", "12345", Role.CUSTOMER);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingRole_returns400() throws Exception {
        String json = "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"password123\",\"phone\":\"9876543210\"}";

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "123", "9876543210", Role.CUSTOMER);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123", "9876543210", Role.CUSTOMER);
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Email already registered: john@example.com"));

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_invalidRoleValue_returns400() throws Exception {
        String json = "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"password123\",\"phone\":\"9876543210\",\"role\":\"ADMIN\"}";

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-valid-json"))
                .andExpect(status().isBadRequest());
    }
}
