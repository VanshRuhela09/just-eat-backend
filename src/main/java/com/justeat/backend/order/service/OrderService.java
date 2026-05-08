package com.justeat.backend.order.service;

import com.justeat.backend.order.dto.OrderRequest;
import com.justeat.backend.order.dto.OrderResponse;
import com.justeat.backend.order.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request);
    List<OrderResponse> getMyOrders();
    OrderResponse getOrderById(Long orderId);
    List<OrderResponse> getRestaurantOrders();
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
    OrderResponse rateOrder(Long orderId, Double rating);
}

