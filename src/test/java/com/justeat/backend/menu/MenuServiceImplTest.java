package com.justeat.backend.menu;

import com.justeat.backend.common.enums.Role;
import com.justeat.backend.menu.dto.MenuItemRequest;
import com.justeat.backend.menu.dto.MenuItemResponse;
import com.justeat.backend.menu.entity.MenuItem;
import com.justeat.backend.menu.repository.MenuItemRepository;
import com.justeat.backend.menu.service.impl.MenuServiceImpl;
import com.justeat.backend.restaurant.entity.Restaurant;
import com.justeat.backend.restaurant.repository.RestaurantRepository;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock private MenuItemRepository menuItemRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private MenuServiceImpl menuService;

    private User ownerUser;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .role(Role.OWNER)
                .build();

        restaurant = Restaurant.builder()
                .id(5L)
                .name("Burger Town")
                .owner(ownerUser)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("owner@example.com");
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
    }

    @Test
    void addMenuItem_success() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Classic Burger");
        request.setDescription("Juicy beef burger");
        request.setPrice(12.99);
        request.setIsAvailable(true);
        request.setIsSpecial(false);
        request.setRestaurantId(5L);

        when(restaurantRepository.findById(5L)).thenReturn(Optional.of(restaurant));

        MenuItem savedItem = MenuItem.builder()
                .id(100L)
                .name("Classic Burger")
                .description("Juicy beef burger")
                .price(12.99)
                .isAvailable(true)
                .isSpecial(false)
                .restaurant(restaurant)
                .build();

        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(savedItem);

        MenuItemResponse response = menuService.addMenuItem(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Classic Burger");
        assertThat(response.getPrice()).isEqualTo(12.99);
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void deleteMenuItem_success() {
        MenuItem menuItem = MenuItem.builder()
                .id(100L)
                .name("Classic Burger")
                .restaurant(restaurant)
                .build();

        when(menuItemRepository.findById(100L)).thenReturn(Optional.of(menuItem));
        when(restaurantRepository.findById(5L)).thenReturn(Optional.of(restaurant));

        menuService.deleteMenuItem(100L);

        verify(menuItemRepository).delete(menuItem);
    }
}


