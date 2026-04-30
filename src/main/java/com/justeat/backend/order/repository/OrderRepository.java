package com.justeat.backend.order.repository;

import com.justeat.backend.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderCreatedAtDesc(Long userId);
    List<Order> findByRestaurantIdOrderByOrderCreatedAtDesc(Long restaurantId);
}

