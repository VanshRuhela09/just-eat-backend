package com.justeat.backend.user.service.impl;

import com.justeat.backend.user.dto.UpdateUserRequest;
import com.justeat.backend.user.dto.UserResponse;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import com.justeat.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Extracts the authenticated user's email from SecurityContextHolder
     * and fetches the user from the database.
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Maps a User entity to a UserResponse DTO (never exposes password).
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserResponse getLoggedInUser() {
        User user = getAuthenticatedUser();
        return mapToResponse(user);
    }

    @Override
    public UserResponse updateLoggedInUser(UpdateUserRequest request) {
        User user = getAuthenticatedUser();

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        // Update password if provided (encode it)
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Email and Role are NOT updatable from this API
        userRepository.save(user);
        return mapToResponse(user);
    }
}

