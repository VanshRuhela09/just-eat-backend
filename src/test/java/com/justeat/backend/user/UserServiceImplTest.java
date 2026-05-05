package com.justeat.backend.user;

import com.justeat.backend.common.enums.Role;
import com.justeat.backend.user.dto.UpdateUserRequest;
import com.justeat.backend.user.dto.UserResponse;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import com.justeat.backend.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .password("encoded")
                .role(Role.CUSTOMER)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("jane@example.com");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(mockUser));
    }

    @Test
    void getLoggedInUser_returnsUserResponse() {
        UserResponse response = userService.getLoggedInUser();

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getName()).isEqualTo("Jane Doe");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void updateLoggedInUser_updatesNameAndPassword() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Jane Updated");
        request.setPassword("newpass");

        when(passwordEncoder.encode("newpass")).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse response = userService.updateLoggedInUser(request);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("newpass");
    }
}

