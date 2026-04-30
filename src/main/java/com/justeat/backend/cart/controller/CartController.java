package com.justeat.backend.cart.controller;

import com.justeat.backend.cart.dto.CartItemRequest;
import com.justeat.backend.cart.dto.CartResponse;
import com.justeat.backend.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;


    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }


    @PostMapping
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(itemId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(itemId));
    }


    @DeleteMapping
    public ResponseEntity<String> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok("Cart cleared successfully.");
    }
}