package com.justeat.backend.menu.controller;

import com.justeat.backend.menu.dto.GlobalPopularItemResponse;
import com.justeat.backend.menu.entity.MenuItem;
import com.justeat.backend.menu.service.PopularityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for platform-wide (global) popular menu items.
 * Accessible by all authenticated users.
 *
 * Scope: GLOBAL — aggregates across ALL restaurants.
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuPopularityController {

    private final PopularityService popularityService;

    /**
     * GET /menu/popular
     * Returns globally trending menu items across all restaurants.
     * Items are sorted by total order count DESC.
     * Accessible by all users (customers, owners).
     */
    @GetMapping("/popular")
    public ResponseEntity<List<GlobalPopularItemResponse>> getGlobalPopularItems() {
        List<MenuItem> popularItems = popularityService.getGlobalPopularItems();
        List<GlobalPopularItemResponse> response = popularItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private GlobalPopularItemResponse mapToResponse(MenuItem item) {
        return GlobalPopularItemResponse.builder()
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

