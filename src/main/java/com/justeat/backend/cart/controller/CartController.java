package com.justeat.backend.cart.controller;

import com.justeat.backend.cart.dto.CartItemRequest;
import com.justeat.backend.cart.dto.CartResponse;
import com.justeat.backend.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "Cart", description = "Shopping cart management (CUSTOMER only)")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get the current user's cart")
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @Operation(summary = "Add an item to the cart")
    @PostMapping
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(request));
    }

    @Operation(summary = "Update quantity of a cart item")
    @PutMapping("/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @Parameter(description = "Cart item ID") @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(itemId, request));
    }

    @Operation(summary = "Remove a specific item from the cart")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@Parameter(description = "Cart item ID") @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(itemId));
    }

    @Operation(summary = "Clear all items from the cart")
    @DeleteMapping
    public ResponseEntity<String> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok("Cart cleared successfully.");
    }
}