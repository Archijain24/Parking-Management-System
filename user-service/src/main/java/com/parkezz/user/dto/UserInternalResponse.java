package com.parkezz.user.dto;

import com.parkezz.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInternalResponse {
    private Long userId;
    private String name;
    private String email;
    private String password;
    private Role role;
}
