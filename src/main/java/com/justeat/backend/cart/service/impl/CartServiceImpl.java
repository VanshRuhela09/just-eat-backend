package com.justeat.backend.cart.service.impl;

import com.justeat.backend.cart.dto.CartItemRequest;
import com.justeat.backend.cart.dto.CartItemResponse;
import com.justeat.backend.cart.dto.CartResponse;
import com.justeat.backend.cart.entity.Cart;
import com.justeat.backend.cart.entity.CartItem;
import com.justeat.backend.cart.repository.CartItemRepository;
import com.justeat.backend.cart.repository.CartRepository;
import com.justeat.backend.cart.service.CartService;
import com.justeat.backend.menu.entity.MenuItem;
import com.justeat.backend.menu.repository.MenuItemRepository;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;


    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + email));
    }


    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Map Cart entity to CartResponse DTO with total price calculation.
     */
    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .itemId(item.getId())
                        .menuItemId(item.getMenuItem().getId())
                        .name(item.getMenuItem().getName())
                        .price(item.getMenuItem().getPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getMenuItem().getPrice() * item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        Double totalCartPrice = itemResponses.stream()
                .mapToDouble(CartItemResponse::getTotalPrice)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .userEmail(cart.getUser().getEmail())
                .items(itemResponses)
                .totalCartPrice(totalCartPrice)
                .build();
    }

    @Override
    public CartResponse getCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse addItem(CartItemRequest request) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        log.info("Adding menu item id: {} to cart for user: {}", request.getMenuItemId(), user.getEmail());

        // Fetch and validate menu item
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + request.getMenuItemId()));

        if (!menuItem.getIsAvailable()) {
            throw new RuntimeException("Menu item '" + menuItem.getName() + "' is currently not available.");
        }

        // If item already exists in cart → increase quantity
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndMenuItemId(cart.getId(), menuItem.getId());

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            // Create new CartItem
            CartItem newItem = CartItem.builder()
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .cart(cart)
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return mapToResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Override
    public CartResponse updateItemQuantity(Long cartItemId, CartItemRequest request) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        log.info("Updating cart item id: {} quantity to {} for user: {}", cartItemId, request.getQuantity(), user.getEmail());

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));

        // Ensure cart item belongs to logged-in user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Unauthorized: This cart item does not belong to your cart.");
        }

        // If quantity = 0 → remove item
        if (request.getQuantity() == 0) {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
        }

        return mapToResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Override
    public CartResponse removeItem(Long cartItemId) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        log.info("Removing cart item id: {} for user: {}", cartItemId, user.getEmail());

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));

        // Ensure cart item belongs to logged-in user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Unauthorized: This cart item does not belong to your cart.");
        }

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return mapToResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Override
    public void clearCart() {
        User user = getAuthenticatedUser();
        log.info("Clearing cart for user: {}", user.getEmail());
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}

