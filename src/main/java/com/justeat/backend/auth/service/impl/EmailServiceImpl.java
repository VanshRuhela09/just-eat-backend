package com.justeat.backend.auth.service.impl;

import com.justeat.backend.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request - Just Eat");
        message.setText(buildEmailBody(resetLink));

        try {
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }

    private String buildEmailBody(String resetLink) {
        return """
            Hello,
            
            We received a request to reset your password for your Just Eat account.
            
            Click the link below to reset your password:
            %s
            
            This link will expire in 30 minutes.
            
            If you did not request a password reset, please ignore this email or contact support if you have concerns.
            
            Best regards,
            The Just Eat Team
            """.formatted(resetLink);
    }
}

