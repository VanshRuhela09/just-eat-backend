package com.justeat.backend.user.service.impl;

import com.justeat.backend.restaurant.repository.RestaurantRepository;
import com.justeat.backend.user.dto.UserPreferencesRequest;
import com.justeat.backend.user.dto.UserPreferencesResponse;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.entity.UserPreferences;
import com.justeat.backend.user.enums.CuisineType;
import com.justeat.backend.user.enums.DietaryRestriction;
import com.justeat.backend.user.repository.UserPreferencesRepository;
import com.justeat.backend.user.repository.UserRepository;
import com.justeat.backend.user.service.UserPreferencesService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPreferencesServiceImpl implements UserPreferencesService {

    private final UserPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Get the currently authenticated user from SecurityContext.
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Get or create preferences for a user.
     */
    private UserPreferences getOrCreatePreferences(User user) {
        return preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserPreferences newPrefs = UserPreferences.builder()
                            .user(user)
                            .favouriteRestaurantIds(new HashSet<>())
                            .favouriteCuisines(new HashSet<>())
                            .dietaryRestrictions(new HashSet<>())
                            .build();
                    return preferencesRepository.save(newPrefs);
                });
    }

    /**
     * Map entity to response DTO.
     */
    private UserPreferencesResponse mapToResponse(UserPreferences prefs) {
        return UserPreferencesResponse.builder()
                .userId(prefs.getUser().getId())
                .favouriteRestaurantIds(prefs.getFavouriteRestaurantIds())
                .favouriteCuisines(prefs.getFavouriteCuisines())
                .dietaryRestrictions(prefs.getDietaryRestrictions())
                .build();
    }

    /**
     * Validate that the cuisine is a valid CuisineType enum value.
     */
    private String validateAndNormalizeCuisine(String cuisine) {
        if (cuisine == null || cuisine.trim().isEmpty()) {
            throw new IllegalArgumentException("Cuisine cannot be empty.");
        }

        String normalized = cuisine.toUpperCase().trim().replace(" ", "_").replace("-", "_");

        try {
            CuisineType.valueOf(normalized);
            return normalized;
        } catch (IllegalArgumentException e) {
            String validCuisines = Arrays.stream(CuisineType.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Invalid cuisine: '" + cuisine + "'. Valid options are: " + validCuisines);
        }
    }

    /**
     * Validate that the restriction is a valid DietaryRestriction enum value.
     */
    private String validateAndNormalizeRestriction(String restriction) {
        if (restriction == null || restriction.trim().isEmpty()) {
            throw new IllegalArgumentException("Dietary restriction cannot be empty.");
        }

        String normalized = restriction.toUpperCase().trim().replace(" ", "_").replace("-", "_");

        try {
            DietaryRestriction.valueOf(normalized);
            return normalized;
        } catch (IllegalArgumentException e) {
            String validRestrictions = Arrays.stream(DietaryRestriction.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Invalid dietary restriction: '" + restriction + "'. Valid options are: " + validRestrictions);
        }
    }

    @Override
    public UserPreferencesResponse getPreferences() {
        User user = getAuthenticatedUser();
        UserPreferences prefs = getOrCreatePreferences(user);
        return mapToResponse(prefs);
    }

    @Override
    public UserPreferencesResponse savePreferences(UserPreferencesRequest request) {
        User user = getAuthenticatedUser();
        UserPreferences prefs = getOrCreatePreferences(user);

        if (request.getFavouriteRestaurantIds() != null) {
            Set<Long> validIds = request.getFavouriteRestaurantIds().stream()
                    .filter(restaurantRepository::existsById)
                    .collect(Collectors.toSet());

            Set<Long> invalidIds = new HashSet<>(request.getFavouriteRestaurantIds());
            invalidIds.removeAll(validIds);

            if (!invalidIds.isEmpty()) {
                throw new EntityNotFoundException("Restaurants not found with ids: " + invalidIds);
            }
            prefs.setFavouriteRestaurantIds(request.getFavouriteRestaurantIds());
        }

        if (request.getFavouriteCuisines() != null) {
            Set<String> normalizedCuisines = request.getFavouriteCuisines().stream()
                    .map(this::validateAndNormalizeCuisine)
                    .collect(Collectors.toSet());
            prefs.setFavouriteCuisines(normalizedCuisines);
        }

        // Validate and set dietary restrictions
        if (request.getDietaryRestrictions() != null) {
            Set<String> normalizedRestrictions = request.getDietaryRestrictions().stream()
                    .map(this::validateAndNormalizeRestriction)
                    .collect(Collectors.toSet());
            prefs.setDietaryRestrictions(normalizedRestrictions);
        }

        return mapToResponse(preferencesRepository.save(prefs));
    }

    @Override
    public void addFavouriteRestaurant(Long restaurantId) {
        // Validate restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new EntityNotFoundException("Restaurant not found with id: " + restaurantId);
        }

        User user = getAuthenticatedUser();
        UserPreferences prefs = getOrCreatePreferences(user);

        if (prefs.getFavouriteRestaurantIds().contains(restaurantId)) {
            throw new IllegalArgumentException("Restaurant with id " + restaurantId + " is already in your favourites.");
        }

        prefs.getFavouriteRestaurantIds().add(restaurantId);
        preferencesRepository.save(prefs);
    }

    @Override
    public void removeFavouriteRestaurant(Long restaurantId) {
        User user = getAuthenticatedUser();
        UserPreferences prefs = preferencesRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("No preferences found for user."));

        if (!prefs.getFavouriteRestaurantIds().contains(restaurantId)) {
            throw new EntityNotFoundException("Restaurant with id " + restaurantId + " is not in your favourites.");
        }

        prefs.getFavouriteRestaurantIds().remove(restaurantId);
        preferencesRepository.save(prefs);
    }

    @Override
    public void addFavouriteCuisine(String cuisine) {
        String normalizedCuisine = validateAndNormalizeCuisine(cuisine);

        User user = getAuthenticatedUser();
        UserPreferences prefs = getOrCreatePreferences(user);

        if (prefs.getFavouriteCuisines().contains(normalizedCuisine)) {
            throw new IllegalArgumentException("Cuisine '" + cuisine + "' is already in your favourites.");
        }

        prefs.getFavouriteCuisines().add(normalizedCuisine);
        preferencesRepository.save(prefs);
    }

    @Override
    public void removeFavouriteCuisine(String cuisine) {
        String normalizedCuisine = validateAndNormalizeCuisine(cuisine);

        User user = getAuthenticatedUser();
        UserPreferences prefs = preferencesRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("No preferences found for user."));


        if (!prefs.getFavouriteCuisines().contains(normalizedCuisine)) {
            throw new EntityNotFoundException("Cuisine '" + cuisine + "' is not in your favourites.");
        }

        prefs.getFavouriteCuisines().remove(normalizedCuisine);
        preferencesRepository.save(prefs);
    }

    @Override
    public void addDietaryRestriction(String restriction) {
        String normalizedRestriction = validateAndNormalizeRestriction(restriction);

        User user = getAuthenticatedUser();
        UserPreferences prefs = getOrCreatePreferences(user);

        if (prefs.getDietaryRestrictions().contains(normalizedRestriction)) {
            throw new IllegalArgumentException("Dietary restriction '" + restriction + "' is already added.");
        }

        prefs.getDietaryRestrictions().add(normalizedRestriction);
        preferencesRepository.save(prefs);
    }

    @Override
    public void removeDietaryRestriction(String restriction) {
        String normalizedRestriction = validateAndNormalizeRestriction(restriction);

        User user = getAuthenticatedUser();
        UserPreferences prefs = preferencesRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("No preferences found for user."));


        if (!prefs.getDietaryRestrictions().contains(normalizedRestriction)) {
            throw new EntityNotFoundException("Dietary restriction '" + restriction + "' is not in your preferences.");
        }

        prefs.getDietaryRestrictions().remove(normalizedRestriction);
        preferencesRepository.save(prefs);
    }
}

