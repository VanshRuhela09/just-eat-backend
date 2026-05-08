package com.justeat.backend.config.security;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = Collections.synchronizedSet(new HashSet<>());

    public void addToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}

