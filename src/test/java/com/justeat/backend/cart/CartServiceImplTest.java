package com.justeat.backend.cart;

import com.justeat.backend.cart.dto.CartItemRequest;
import com.justeat.backend.cart.dto.CartResponse;
import com.justeat.backend.cart.entity.Cart;
import com.justeat.backend.cart.entity.CartItem;
import com.justeat.backend.cart.repository.CartItemRepository;
import com.justeat.backend.cart.repository.CartRepository;
import com.justeat.backend.cart.service.impl.CartServiceImpl;
import com.justeat.backend.common.enums.Role;
import com.justeat.backend.menu.entity.MenuItem;
import com.justeat.backend.menu.repository.MenuItemRepository;
import com.justeat.backend.restaurant.entity.Restaurant;
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

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private CartServiceImpl cartService;

    private User customer;
    private Cart cart;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(1L)
                .name("Customer")
                .email("customer@example.com")
                .role(Role.CUSTOMER)
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(5L)
                .name("Burger Town")
                .build();

        menuItem = MenuItem.builder()
                .id(10L)
                .name("Burger")
                .price(9.99)
                .isAvailable(true)
                .restaurant(restaurant)
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(customer)
                .items(new ArrayList<>())
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("customer@example.com");
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(customer));
    }

    @Test
    void addItem_newItem_success() {
        CartItemRequest request = new CartItemRequest();
        request.setMenuItemId(10L);
        request.setQuantity(2);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));
        when(cartItemRepository.findByCartIdAndMenuItemId(1L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(CartItem.builder()
                .id(1L).menuItem(menuItem).quantity(2).cart(cart).build());
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.addItem(request);

        assertThat(response).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_unavailableItem_throwsException() {
        menuItem.setIsAvailable(false);

        CartItemRequest request = new CartItemRequest();
        request.setMenuItemId(10L);
        request.setQuantity(1);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> cartService.addItem(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void updateItemQuantity_removesItemWhenQuantityIsZero() {
        CartItemRequest request = new CartItemRequest();
        request.setMenuItemId(10L);
        request.setQuantity(0);

        CartItem cartItem = CartItem.builder()
                .id(50L)
                .menuItem(menuItem)
                .quantity(3)
                .cart(cart)
                .build();
        cart.getItems().add(cartItem);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(50L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        cartService.updateItemQuantity(50L, request);

        verify(cartItemRepository).delete(cartItem);
    }
}

