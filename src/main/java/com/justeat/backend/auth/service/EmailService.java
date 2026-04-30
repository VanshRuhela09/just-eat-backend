package com.justeat.backend.auth.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String token);
}

