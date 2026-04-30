package com.justeat.backend.cart.service;

import com.justeat.backend.cart.dto.CartItemRequest;
import com.justeat.backend.cart.dto.CartResponse;

public interface CartService {
    CartResponse getCart();
    CartResponse addItem(CartItemRequest request);
    CartResponse updateItemQuantity(Long cartItemId, CartItemRequest request);
    CartResponse removeItem(Long cartItemId);
    void clearCart();
}

