package com.justeat.backend.cart.repository;

import com.justeat.backend.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndMenuItemId(Long cartId, Long menuItemId);
}

