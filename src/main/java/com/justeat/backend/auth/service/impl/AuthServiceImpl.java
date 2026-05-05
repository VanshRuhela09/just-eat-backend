package com.justeat.backend.auth.service.impl;

import com.justeat.backend.auth.dto.AuthResponse;
import com.justeat.backend.auth.dto.LoginRequest;
import com.justeat.backend.auth.dto.RegisterRequest;
import com.justeat.backend.auth.service.AuthService;
import com.justeat.backend.common.enums.Role;
import com.justeat.backend.config.security.JwtUtil;
import com.justeat.backend.config.security.TokenBlacklistService;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);
        log.info("New CUSTOMER registered: {}", request.getEmail());
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse registerOwner(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Owner registration failed - user already exists: {}", request.getEmail());
            throw new IllegalArgumentException("User already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.OWNER)
                .build();

        userRepository.save(user);
        log.info("New OWNER registered: {}", request.getEmail());
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception ex) {
            log.warn("Login failed for email: {}", request.getEmail());
            throw ex;
        }
        log.info("User logged in: {}", request.getEmail());
        String token = jwtUtil.generateToken(request.getEmail());
        return new AuthResponse(token);
    }

    public ResponseEntity<String> logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Authorization header is missing or invalid. Please provide a valid Bearer token.");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Logout failed.");
        }
        if (tokenBlacklistService.isBlacklisted(token)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Token is already logged out.");
        }
        tokenBlacklistService.addToken(token);
        return ResponseEntity.ok("Logged out successfully.");
    }
}
