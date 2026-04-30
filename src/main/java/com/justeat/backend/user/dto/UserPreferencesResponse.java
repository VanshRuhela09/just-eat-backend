package com.justeat.backend.user.dto;

import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferencesResponse {
    private Long userId;
    private Set<Long> favouriteRestaurantIds;
    private Set<String> favouriteCuisines;
    private Set<String> dietaryRestrictions;
}

