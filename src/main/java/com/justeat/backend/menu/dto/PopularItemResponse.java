package com.justeat.backend.menu.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularItemResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer orderCount;
    private Boolean isPopular;
    private Boolean isAvailable;
    private Long restaurantId;
    private String restaurantName;
}

