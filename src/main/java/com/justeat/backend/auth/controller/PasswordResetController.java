package com.justeat.backend.auth.controller;

import com.justeat.backend.auth.dto.PasswordResetConfirmRequest;
import com.justeat.backend.auth.dto.PasswordResetRequest;
import com.justeat.backend.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Request a password reset. An email with a reset link will be sent if the email exists.
     * For security reasons, the response is always successful regardless of whether the email exists.
     */
    @PostMapping("/forgot")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestPasswordReset(request);
        return ResponseEntity.ok(Map.of(
                "message", "If an account exists with this email, a password reset link has been sent."
        ));
    }

    /**
     * Validate a password reset token.
     */
    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateToken(token);
        return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "message", isValid ? "Token is valid." : "Token is invalid or expired."
        ));
    }

    /**
     * Reset the password using the token received via email.
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmPasswordReset(request);
        return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully. You can now log in with your new password."
        ));
    }
}

