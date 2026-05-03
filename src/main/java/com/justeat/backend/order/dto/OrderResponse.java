package com.justeat.backend.order.dto;

import com.justeat.backend.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String customerName;
    private String customerEmail;
    private String restaurantName;
    private Long restaurantId;
    private List<OrderItemResponse> items;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private Double rating;
}

