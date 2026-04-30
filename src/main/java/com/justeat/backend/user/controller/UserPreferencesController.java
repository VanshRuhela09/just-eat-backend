package com.justeat.backend.user.controller;

import com.justeat.backend.user.dto.UserPreferencesRequest;
import com.justeat.backend.user.dto.UserPreferencesResponse;
import com.justeat.backend.user.enums.CuisineType;
import com.justeat.backend.user.enums.DietaryRestriction;
import com.justeat.backend.user.service.UserPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/user/preferences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class UserPreferencesController {

    private final UserPreferencesService preferencesService;

    /**
     * GET /user/preferences/options
     * Get all valid cuisine and dietary restriction options.
     */
    @GetMapping("/options")
    public ResponseEntity<Map<String, Object>> getValidOptions() {
        return ResponseEntity.ok(Map.of(
                "cuisines", Arrays.stream(CuisineType.values()).map(Enum::name).toList(),
                "dietaryRestrictions", Arrays.stream(DietaryRestriction.values()).map(Enum::name).toList()
        ));
    }

    /**
     * GET /user/preferences
     * Get all preferences for the logged-in user.
     */
    @GetMapping
    public ResponseEntity<UserPreferencesResponse> getPreferences() {
        return ResponseEntity.ok(preferencesService.getPreferences());
    }

    /**
     * PUT /user/preferences
     * Save/update all preferences for the logged-in user.
     */
    @PutMapping
    public ResponseEntity<UserPreferencesResponse> savePreferences(@RequestBody UserPreferencesRequest request) {
        return ResponseEntity.ok(preferencesService.savePreferences(request));
    }

    // ==================== Favourite Restaurants ====================

    /**
     * POST /user/preferences/restaurants/{restaurantId}
     * Add a restaurant to favourites.
     */
    @PostMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Map<String, String>> addFavouriteRestaurant(@PathVariable Long restaurantId) {
        preferencesService.addFavouriteRestaurant(restaurantId);
        return ResponseEntity.ok(Map.of("message", "Restaurant added to favourites."));
    }

    /**
     * DELETE /user/preferences/restaurants/{restaurantId}
     * Remove a restaurant from favourites.
     */
    @DeleteMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Map<String, String>> removeFavouriteRestaurant(@PathVariable Long restaurantId) {
        preferencesService.removeFavouriteRestaurant(restaurantId);
        return ResponseEntity.ok(Map.of("message", "Restaurant removed from favourites."));
    }

    // ==================== Favourite Cuisines ====================

    /**
     * POST /user/preferences/cuisines/{cuisine}
     * Add a cuisine to favourites.
     */
    @PostMapping("/cuisines/{cuisine}")
    public ResponseEntity<Map<String, String>> addFavouriteCuisine(@PathVariable String cuisine) {
        preferencesService.addFavouriteCuisine(cuisine);
        return ResponseEntity.ok(Map.of("message", "Cuisine added to favourites."));
    }

    /**
     * DELETE /user/preferences/cuisines/{cuisine}
     * Remove a cuisine from favourites.
     */
    @DeleteMapping("/cuisines/{cuisine}")
    public ResponseEntity<Map<String, String>> removeFavouriteCuisine(@PathVariable String cuisine) {
        preferencesService.removeFavouriteCuisine(cuisine);
        return ResponseEntity.ok(Map.of("message", "Cuisine removed from favourites."));
    }

    // ==================== Dietary Restrictions ====================

    /**
     * POST /user/preferences/dietary/{restriction}
     * Add a dietary restriction.
     */
    @PostMapping("/dietary/{restriction}")
    public ResponseEntity<Map<String, String>> addDietaryRestriction(@PathVariable String restriction) {
        preferencesService.addDietaryRestriction(restriction);
        return ResponseEntity.ok(Map.of("message", "Dietary restriction added."));
    }

    /**
     * DELETE /user/preferences/dietary/{restriction}
     * Remove a dietary restriction.
     */
    @DeleteMapping("/dietary/{restriction}")
    public ResponseEntity<Map<String, String>> removeDietaryRestriction(@PathVariable String restriction) {
        preferencesService.removeDietaryRestriction(restriction);
        return ResponseEntity.ok(Map.of("message", "Dietary restriction removed."));
    }
}

