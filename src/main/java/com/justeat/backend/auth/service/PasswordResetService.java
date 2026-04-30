package com.justeat.backend.auth.service;

import com.justeat.backend.auth.dto.PasswordResetConfirmRequest;
import com.justeat.backend.auth.dto.PasswordResetRequest;

public interface PasswordResetService {

    /**
     * Initiates a password reset request by generating a token and sending an email.
     */
    void requestPasswordReset(PasswordResetRequest request);

    /**
     * Confirms the password reset by validating the token and updating the password.
     */
    void confirmPasswordReset(PasswordResetConfirmRequest request);

    /**
     * Validates if a token is valid and not expired.
     */
    boolean validateToken(String token);
}

