package com.justeat.backend.restaurant;

import com.justeat.backend.common.enums.Role;
import com.justeat.backend.restaurant.dto.RestaurantRequest;
import com.justeat.backend.restaurant.dto.RestaurantResponse;
import com.justeat.backend.restaurant.entity.Restaurant;
import com.justeat.backend.restaurant.repository.RestaurantRepository;
import com.justeat.backend.restaurant.repository.RestaurantRatingRepository;
import com.justeat.backend.restaurant.service.impl.RestaurantServiceImpl;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock private RestaurantRepository restaurantRepository;
    @Mock private UserRepository userRepository;
    @Mock private RestaurantRatingRepository restaurantRatingRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    private User ownerUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .role(Role.OWNER)
                .build();

        otherUser = User.builder()
                .id(2L)
                .name("Other")
                .email("other@example.com")
                .role(Role.OWNER)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createRestaurant_success() {
        when(authentication.getName()).thenReturn("owner@example.com");
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));

        RestaurantRequest request = new RestaurantRequest();
        request.setName("Pizza Palace");
        request.setDescription("Best pizza in town");
        request.setLocation("Mumbai");
        request.setCuisine("Italian");

        Restaurant savedRestaurant = Restaurant.builder()
                .id(10L)
                .name("Pizza Palace")
                .description("Best pizza in town")
                .location("Mumbai")
                .cuisine("Italian")
                .rating(0.0)
                .owner(ownerUser)
                .build();

        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(savedRestaurant);

        RestaurantResponse response = restaurantService.createRestaurant(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Pizza Palace");
        assertThat(response.getId()).isEqualTo(10L);
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void updateRestaurant_unauthorizedUser_throwsAccessDeniedException() {
        when(authentication.getName()).thenReturn("other@example.com");
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

        Restaurant existingRestaurant = Restaurant.builder()
                .id(10L)
                .name("Pizza Palace")
                .owner(ownerUser)
                .build();

        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(existingRestaurant));

        RestaurantRequest request = new RestaurantRequest();
        request.setName("New Name");

        assertThatThrownBy(() -> restaurantService.updateRestaurant(10L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");

        verify(restaurantRepository, never()).save(any());
    }
}

