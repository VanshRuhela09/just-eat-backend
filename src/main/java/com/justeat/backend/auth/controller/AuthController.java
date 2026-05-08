package com.justeat.backend.auth.controller;

import com.justeat.backend.auth.dto.AuthResponse;
import com.justeat.backend.auth.dto.LoginRequest;
import com.justeat.backend.auth.dto.RegisterRequest;
import com.justeat.backend.auth.service.AuthService;
import com.justeat.backend.config.security.JwtUtil;
import com.justeat.backend.config.security.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login and logout endpoints")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Register a new customer")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Register a new restaurant owner")
    @PostMapping("/owner/register")
    public ResponseEntity<AuthResponse> registerOwner(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerOwner(request));
    }

    @Operation(summary = "Login and receive a JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Logout and invalidate the JWT token")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return authService.logout(authHeader);
    }
}
