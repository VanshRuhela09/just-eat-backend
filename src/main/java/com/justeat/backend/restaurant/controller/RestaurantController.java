package com.justeat.backend.restaurant.controller;

import com.justeat.backend.restaurant.dto.RatingRequest;
import com.justeat.backend.restaurant.dto.RestaurantRequest;
import com.justeat.backend.restaurant.dto.RestaurantResponse;
import com.justeat.backend.restaurant.dto.RestaurantStatusRequest;
import com.justeat.backend.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Restaurant management and search")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @Operation(summary = "Create a new restaurant (OWNER only)")
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(request));
    }

    @Operation(summary = "Update a restaurant by ID (OWNER only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    @Operation(summary = "Update restaurant status (OWNER/ADMIN)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<RestaurantResponse> updateRestaurantStatus(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Valid @RequestBody RestaurantStatusRequest request) {
        return ResponseEntity.ok(restaurantService.updateStatus(id, request));
    }

    @Operation(summary = "Delete a restaurant by ID (OWNER only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<String> deleteRestaurant(@Parameter(description = "Restaurant ID") @PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok("Restaurant deleted successfully.");
    }

    @Operation(summary = "Get all restaurants")
    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @Operation(summary = "Search restaurants by name, location or cuisine")
    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(
            @Parameter(description = "Restaurant name") @RequestParam(required = false) String name,
            @Parameter(description = "Location") @RequestParam(required = false) String location,
            @Parameter(description = "Cuisine type") @RequestParam(required = false) String cuisine) {
        return ResponseEntity.ok(restaurantService.searchRestaurants(name, location, cuisine));
    }
}
