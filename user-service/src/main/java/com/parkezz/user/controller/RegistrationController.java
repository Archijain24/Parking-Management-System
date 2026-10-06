package com.parkezz.user.controller;

import com.parkezz.user.dto.RegisterRequest;
import com.parkezz.user.dto.UserResponse;
import com.parkezz.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Registration", description = "User registration APIs")
public class RegistrationController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user (OWNER or CUSTOMER)")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
