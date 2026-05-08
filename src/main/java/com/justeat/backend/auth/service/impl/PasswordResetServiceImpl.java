package com.justeat.backend.auth.service.impl;

import com.justeat.backend.auth.dto.PasswordResetConfirmRequest;
import com.justeat.backend.auth.dto.PasswordResetRequest;
import com.justeat.backend.auth.entity.PasswordResetToken;
import com.justeat.backend.auth.repository.PasswordResetTokenRepository;
import com.justeat.backend.auth.service.EmailService;
import com.justeat.backend.auth.service.PasswordResetService;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${password-reset.token.expiration-minutes:30}")
    private int tokenExpirationMinutes;

    @Override
    public void requestPasswordReset(PasswordResetRequest request) {
        String email = request.getEmail();

        // Find user by email - we don't reveal if email exists for security
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            // Don't reveal that the email doesn't exist - return silently
            return;
        }

        // Delete any existing tokens for this user
        tokenRepository.deleteByUserId(user.getId());

        // Generate new token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(tokenExpirationMinutes))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Send email with reset link
        emailService.sendPasswordResetEmail(email, token);

        log.info("Password reset token generated and email sent for user: {}", email);
    }

    @Override
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token."));

        // Validate token
        if (resetToken.getUsed()) {
            throw new RuntimeException("This password reset token has already been used.");
        }

        if (resetToken.isExpired()) {
            throw new RuntimeException("This password reset token has expired. Please request a new one.");
        }

        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    @Override
    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(resetToken -> !resetToken.getUsed() && !resetToken.isExpired())
                .orElse(false);
    }
}

