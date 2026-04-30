package com.justeat.backend.order.service;

import com.justeat.backend.order.dto.OrderResponse;
import com.justeat.backend.order.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder();
    List<OrderResponse> getMyOrders();
    OrderResponse getOrderById(Long orderId);
    List<OrderResponse> getRestaurantOrders();
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
}

