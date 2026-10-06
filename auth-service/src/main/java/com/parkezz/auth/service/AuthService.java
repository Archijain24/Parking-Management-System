package com.parkezz.auth.service;

import com.parkezz.auth.dto.LoginRequest;
import com.parkezz.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
