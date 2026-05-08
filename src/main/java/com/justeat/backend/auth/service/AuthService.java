package com.justeat.backend.auth.service;

import com.justeat.backend.auth.dto.AuthResponse;
import com.justeat.backend.auth.dto.LoginRequest;
import com.justeat.backend.auth.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse registerOwner(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    ResponseEntity<String> logout(String authHeader);
}

