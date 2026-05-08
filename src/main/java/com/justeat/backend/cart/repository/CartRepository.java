package com.justeat.backend.cart.repository;

import com.justeat.backend.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT DISTINCT c FROM Cart c " +
            "JOIN FETCH c.user " +
            "LEFT JOIN FETCH c.items i " +
            "LEFT JOIN FETCH i.menuItem m " +
            "LEFT JOIN FETCH m.restaurant " +
            "WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT c FROM Cart c " +
            "JOIN FETCH c.user " +
            "LEFT JOIN FETCH c.items i " +
            "LEFT JOIN FETCH i.menuItem m " +
            "LEFT JOIN FETCH m.restaurant " +
            "WHERE c.id = :id")
    Optional<Cart> findById(@Param("id") Long id);
}
