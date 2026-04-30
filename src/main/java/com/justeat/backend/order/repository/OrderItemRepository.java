package com.justeat.backend.order.repository;

import com.justeat.backend.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.menuItem.id, SUM(oi.quantity) as totalOrders " +
           "FROM OrderItem oi " +
           "WHERE oi.order.orderCreatedAt >= :since " +
           "GROUP BY oi.menuItem.id")
    List<Object[]> getOrderCountsByMenuItem(@Param("since") LocalDateTime since);

    @Query("SELECT oi.menuItem.id, SUM(oi.quantity) as totalOrders " +
           "FROM OrderItem oi " +
           "WHERE oi.menuItem.restaurant.id = :restaurantId AND oi.order.orderCreatedAt >= :since " +
           "GROUP BY oi.menuItem.id " +
           "ORDER BY totalOrders DESC")
    List<Object[]> getOrderCountsByRestaurant(
            @Param("restaurantId") Long restaurantId,
            @Param("since") LocalDateTime since);
}

