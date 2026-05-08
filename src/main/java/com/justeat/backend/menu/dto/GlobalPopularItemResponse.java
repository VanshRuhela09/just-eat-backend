package com.justeat.backend.menu.dto;

import lombok.*;

/**
 * DTO for globally popular items (platform-wide trending, across all restaurants).
 * Separate from restaurant-scoped PopularItemResponse to avoid mixing scopes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalPopularItemResponse {
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

