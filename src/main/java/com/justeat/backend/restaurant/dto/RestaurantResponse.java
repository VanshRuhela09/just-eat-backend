package com.justeat.backend.restaurant.dto;

import com.justeat.backend.restaurant.enums.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String cuisine;
    private String location;
    private Double rating;
    private RestaurantStatus status;
    private String ownerName;
    private String ownerEmail;
    private String imageUrl;
}

