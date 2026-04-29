package com.justeat.backend.restaurant.dto;

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
    private String ownerName;
    private String ownerEmail;
}

