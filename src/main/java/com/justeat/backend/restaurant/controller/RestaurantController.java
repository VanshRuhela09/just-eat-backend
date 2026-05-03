package com.justeat.backend.restaurant.controller;

import com.justeat.backend.restaurant.dto.RestaurantRequest;
import com.justeat.backend.restaurant.dto.RestaurantResponse;
import com.justeat.backend.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.justeat.backend.restaurant.dto.RatingRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;


    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<String> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok("Restaurant deleted successfully.");
    }


    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }


    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String cuisine) {
        return ResponseEntity.ok(restaurantService.searchRestaurants(name, location, cuisine));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/{restaurantId}/rating")
    public ResponseEntity<?> updateRating(
            @Valid
            @PathVariable Long restaurantId,
            @RequestBody RatingRequest ratingRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        restaurantService.updateRating(restaurantId, userDetails.getUsername(), ratingRequest.getRating());
        return ResponseEntity.ok("Rating updated.");
    }

}

