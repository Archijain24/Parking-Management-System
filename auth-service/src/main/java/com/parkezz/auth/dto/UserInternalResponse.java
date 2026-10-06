package com.parkezz.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors User Service's internal DTO. Kept as a separate simple class here
 * (no shared library) so that Auth Service remains fully independent and
 * deployable on its own, per the microservices "own your data/contracts" idea.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInternalResponse {
    private Long userId;
    private String name;
    private String email;
    private String password;
    private String role;
}
