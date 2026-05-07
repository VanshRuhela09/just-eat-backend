package com.justeat.backend.order.repository;

import com.justeat.backend.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.user " +
            "JOIN FETCH o.restaurant r " +
            "JOIN FETCH r.owner " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.menuItem " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.orderCreatedAt DESC")
    List<Order> findByUserIdOrderByOrderCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.user " +
            "JOIN FETCH o.restaurant r " +
            "JOIN FETCH r.owner " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.menuItem " +
            "WHERE o.restaurant.id = :restaurantId " +
            "ORDER BY o.orderCreatedAt DESC")
    List<Order> findByRestaurantIdOrderByOrderCreatedAtDesc(@Param("restaurantId") Long restaurantId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.user " +
            "JOIN FETCH o.restaurant r " +
            "JOIN FETCH r.owner " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.menuItem " +
            "WHERE o.id = :id")
    Optional<Order> findById(@Param("id") Long id);
}
