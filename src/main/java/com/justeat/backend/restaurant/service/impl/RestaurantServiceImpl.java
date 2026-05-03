package com.justeat.backend.restaurant.service.impl;

import com.justeat.backend.restaurant.dto.RestaurantRequest;
import com.justeat.backend.restaurant.dto.RestaurantResponse;
import com.justeat.backend.restaurant.entity.Restaurant;
import com.justeat.backend.restaurant.repository.RestaurantRepository;
import com.justeat.backend.restaurant.service.RestaurantService;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.justeat.backend.restaurant.entity.RestaurantRating;
import com.justeat.backend.restaurant.repository.RestaurantRatingRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final RestaurantRatingRepository restaurantRatingRepository;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + email));
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .cuisine(restaurant.getCuisine())
                .location(restaurant.getLocation())
                .rating(restaurant.getRating())
                .ownerName(restaurant.getOwner().getName())
                .ownerEmail(restaurant.getOwner().getEmail())
                .build();
    }

    @Override
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        User owner = getAuthenticatedUser();

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .cuisine(request.getCuisine())
                .rating(0.0)
                .owner(owner)
                .build();

        return mapToResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        User owner = getAuthenticatedUser();

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        // Ensure the logged-in user is the owner of this restaurant
        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You are not authorized to update this restaurant.");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            restaurant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            restaurant.setDescription(request.getDescription());
        }
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            restaurant.setLocation(request.getLocation());
        }
        if (request.getCuisine() != null && !request.getCuisine().isBlank()) {
            restaurant.setCuisine(request.getCuisine());
        }

        return mapToResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public void deleteRestaurant(Long id) {
        User owner = getAuthenticatedUser();

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        // Ensure the logged-in user is the owner of this restaurant
        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this restaurant.");
        }

        restaurantRepository.delete(restaurant);
    }

    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantResponse> searchRestaurants(String name, String location, String cuisine) {
        return restaurantRepository.search(name, location, cuisine)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

}

