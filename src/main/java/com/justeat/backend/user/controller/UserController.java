package com.justeat.backend.user.controller;

import com.justeat.backend.user.dto.UpdateUserRequest;
import com.justeat.backend.user.dto.UserResponse;
import com.justeat.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /user/me
     * Returns the profile of the currently authenticated user.
     * Requires valid JWT token in Authorization header.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile() {
        return ResponseEntity.ok(userService.getLoggedInUser());
    }

    /**
     * PUT /user/me
     * Updates the profile of the currently authenticated user.
     * Only name and password can be updated.
     * Email and role cannot be changed.
     * Requires valid JWT token in Authorization header.
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateLoggedInUser(request));
    }
}

