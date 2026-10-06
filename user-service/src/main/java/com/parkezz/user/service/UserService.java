package com.parkezz.user.service;

import com.parkezz.user.dto.RegisterRequest;
import com.parkezz.user.dto.UserInternalResponse;
import com.parkezz.user.dto.UserResponse;
import com.parkezz.user.dto.UserUpdateRequest;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserInternalResponse getInternalByEmail(String email);
    UserResponse getById(Long id, Long requesterId);
    UserResponse update(Long id, UserUpdateRequest request, Long requesterId);
    void delete(Long id, Long requesterId);
}
