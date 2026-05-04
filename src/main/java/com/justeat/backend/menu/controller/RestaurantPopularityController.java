package com.justeat.backend.menu.controller;

import com.justeat.backend.menu.dto.PopularItemResponse;
import com.justeat.backend.menu.entity.MenuItem;
import com.justeat.backend.menu.service.PopularityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for restaurant-scoped popular items (internal best sellers).
 * Scope: RESTAURANT — considers only items within the given restaurant.
 *
 * Popularity is updated in real-time on order placement.
 * No manual recalculation endpoints are exposed.
 */
@RestController
@RequestMapping("/restaurants/{restaurantId}/popular-items")
@RequiredArgsConstructor
public class RestaurantPopularityController {

    private final PopularityService popularityService;

    /**
     * GET /restaurants/{restaurantId}/popular-items
     * Returns popular menu items for the given restaurant.
     * Accessible by all authenticated users.
     */
    @GetMapping
    public ResponseEntity<List<PopularItemResponse>> getPopularItems(
            @PathVariable Long restaurantId) {
        List<MenuItem> popularItems = popularityService.getPopularItemsForRestaurant(restaurantId);
        List<PopularItemResponse> response = popularItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private PopularItemResponse mapToResponse(MenuItem item) {
        return PopularItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .orderCount(item.getOrderCount())
                .isPopular(item.getIsPopular())
                .isAvailable(item.getIsAvailable())
                .restaurantId(item.getRestaurant().getId())
                .restaurantName(item.getRestaurant().getName())
                .build();
    }
}

