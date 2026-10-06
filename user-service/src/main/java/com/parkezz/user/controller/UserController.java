package com.parkezz.user.controller;

import com.parkezz.user.dto.UserInternalResponse;
import com.parkezz.user.dto.UserResponse;
import com.parkezz.user.dto.UserUpdateRequest;
import com.parkezz.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User lookup APIs (public + internal)")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a user's public profile by ID")
    public UserResponse getById(@PathVariable Long id, Authentication authentication) {
        Long requesterId = Long.parseLong(authentication.getName());
        return userService.getById(id, requesterId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update your own profile")
    public UserResponse update(@PathVariable Long id,
                               @Valid @RequestBody UserUpdateRequest request,
                               Authentication authentication) {
        Long requesterId = Long.parseLong(authentication.getName());
        return userService.update(id, request, requesterId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete your own profile")
    public org.springframework.http.ResponseEntity<Void> delete(@PathVariable Long id,
                                                                Authentication authentication) {
        Long requesterId = Long.parseLong(authentication.getName());
        userService.delete(id, requesterId);
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    /**
     * INTERNAL endpoint - called only by Auth Service during login to fetch the
     * hashed password for comparison. This keeps Auth Service from touching
     * this service's database directly. Not intended for external clients.
     */
    @GetMapping("/internal/by-email/{email}")
    @Operation(summary = "[Internal] Get full user record (including hashed password) by email, used by Auth Service")
    public UserInternalResponse getInternalByEmail(@PathVariable String email) {
        return userService.getInternalByEmail(email);
    }
}
