package com.parkezz.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkezz.user.config.SecurityConfig;
import com.parkezz.user.dto.UserResponse;
import com.parkezz.user.dto.UserUpdateRequest;
import com.parkezz.user.entity.Role;
import com.parkezz.user.exception.ResourceNotFoundException;
import com.parkezz.user.exception.UnauthorizedActionException;
import com.parkezz.user.security.JwtFilter;
import com.parkezz.user.security.JwtService;
import com.parkezz.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Runs with the real SecurityConfig/JwtFilter/JwtService beans (not mocked)
 * so that the "authenticated()" rule for /api/users/** actually executes,
 * while the caller's identity is injected directly via the
 * SecurityMockMvcRequestPostProcessors.authentication(...) helper instead of
 * a real JWT - this exercises the controller + authorization logic without
 * needing a real token.
 */
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtFilter.class, JwtService.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private static UsernamePasswordAuthenticationToken asUser(long userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                String.valueOf(userId), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void getById_ownProfile_returns200() throws Exception {
        UserResponse response = UserResponse.builder()
                .userId(1L).name("John").email("john@example.com").phone("9876543210").role(Role.CUSTOMER).build();
        when(userService.getById(eq(1L), eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/users/1").with(authentication(asUser(1L, "CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getById_noAuthentication_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status).isIn(401, 403);
                });
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(userService.getById(eq(1L), eq(1L)))
                .thenThrow(new ResourceNotFoundException("User not found with id: 1"));

        mockMvc.perform(get("/api/users/1").with(authentication(asUser(1L, "CUSTOMER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_anotherUsersProfile_returns403() throws Exception {
        when(userService.getById(eq(2L), eq(1L)))
                .thenThrow(new UnauthorizedActionException("You can only view your own profile"));

        mockMvc.perform(get("/api/users/2").with(authentication(asUser(1L, "CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_validRequest_returns200() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("John Updated", "john@example.com", "9876543210");
        UserResponse response = UserResponse.builder()
                .userId(1L).name("John Updated").email("john@example.com").phone("9876543210").role(Role.CUSTOMER).build();
        when(userService.update(eq(1L), any(UserUpdateRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .with(authentication(asUser(1L, "CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));
    }

    @Test
    void update_invalidEmail_returns400() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("John", "not-an-email", "9876543210");

        mockMvc.perform(put("/api/users/1")
                        .with(authentication(asUser(1L, "CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_ownProfile_returns204() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(authentication(asUser(1L, "CUSTOMER"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("User not found with id: 1"))
                .when(userService).delete(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/users/1").with(authentication(asUser(1L, "CUSTOMER"))))
                .andExpect(status().isNotFound());
    }
}
