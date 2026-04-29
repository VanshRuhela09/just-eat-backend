package com.justeat.backend.auth.controller;

import com.justeat.backend.auth.dto.AuthResponse;
import com.justeat.backend.auth.dto.LoginRequest;
import com.justeat.backend.auth.dto.RegisterRequest;
import com.justeat.backend.auth.service.AuthService;
import com.justeat.backend.config.security.JwtUtil;
import com.justeat.backend.config.security.TokenBlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Check if Authorization header is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Authorization header is missing or invalid. Please provide a valid Bearer token.");
        }

        String token = authHeader.substring(7);

        // Check if token is a valid JWT
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Logout failed.");
        }

        // Check if token is already blacklisted
        if (tokenBlacklistService.isBlacklisted(token)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Token is already logged out.");
        }

        // Blacklist the token
        tokenBlacklistService.addToken(token);
        return ResponseEntity.ok("Logged out successfully.");
    }
}

