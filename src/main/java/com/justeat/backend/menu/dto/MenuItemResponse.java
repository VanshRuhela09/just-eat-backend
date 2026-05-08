package com.justeat.backend.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Boolean isAvailable;
    private Boolean isSpecial;
    private String restaurantName;
    private String imageUrl;
}

