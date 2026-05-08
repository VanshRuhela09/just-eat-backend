package com.justeat.backend.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long itemId;
    private Long menuItemId;
    private String name;
    private Double price;
    private Integer quantity;
    private Double totalPrice;
}

