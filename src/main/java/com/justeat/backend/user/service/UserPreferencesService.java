package com.justeat.backend.user.service;

import com.justeat.backend.user.dto.UserPreferencesRequest;
import com.justeat.backend.user.dto.UserPreferencesResponse;

public interface UserPreferencesService {

    /**
     * Get preferences for the currently logged-in user.
     */
    UserPreferencesResponse getPreferences();

    /**
     * Save/update all preferences for the currently logged-in user.
     */
    UserPreferencesResponse savePreferences(UserPreferencesRequest request);

    /**
     * Add a restaurant to favourites.
     */
    void addFavouriteRestaurant(Long restaurantId);

    /**
     * Remove a restaurant from favourites.
     */
    void removeFavouriteRestaurant(Long restaurantId);

    /**
     * Add a cuisine to favourites.
     */
    void addFavouriteCuisine(String cuisine);

    /**
     * Remove a cuisine from favourites.
     */
    void removeFavouriteCuisine(String cuisine);

    /**
     * Add a dietary restriction.
     */
    void addDietaryRestriction(String restriction);

    /**
     * Remove a dietary restriction.
     */
    void removeDietaryRestriction(String restriction);
}

