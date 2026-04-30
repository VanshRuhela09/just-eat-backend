package com.justeat.backend.menu.controller;

import com.justeat.backend.menu.dto.PopularItemResponse;
import com.justeat.backend.menu.entity.MenuItem;
import com.justeat.backend.menu.service.PopularityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/restaurants/{restaurantId}/popular-items")
@RequiredArgsConstructor
public class PopularItemController {

    private final PopularityService popularityService;

    /**
     * GET /restaurants/{restaurantId}/popular-items
     * Get all popular (mostly ordered) items for a restaurant.
     * Accessible by restaurant owner and customers.
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

    /**
     * GET /restaurants/{restaurantId}/popular-items/all
     * Get all items sorted by order count (most ordered first).
     * For restaurant owner to see the ranking.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<PopularItemResponse>> getMostOrderedItems(
            @PathVariable Long restaurantId) {
        List<MenuItem> items = popularityService.getMostOrderedItemsForRestaurant(restaurantId);
        List<PopularItemResponse> response = items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /restaurants/{restaurantId}/popular-items/recalculate
     * Manually trigger popularity recalculation for a restaurant.
     * Only accessible by restaurant owner.
     */
    @PostMapping("/recalculate")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Map<String, String>> recalculatePopularity(
            @PathVariable Long restaurantId) {
        popularityService.calculatePopularityForRestaurant(restaurantId);
        return ResponseEntity.ok(Map.of(
                "message", "Popularity recalculated successfully for restaurant " + restaurantId
        ));
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

