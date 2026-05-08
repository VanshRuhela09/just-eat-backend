package com.justeat.backend.restaurant.service.impl;

import com.justeat.backend.restaurant.dto.RestaurantRequest;
import com.justeat.backend.restaurant.dto.RestaurantResponse;
import com.justeat.backend.restaurant.dto.RestaurantStatusRequest;
import com.justeat.backend.restaurant.entity.Restaurant;
import com.justeat.backend.restaurant.enums.RestaurantStatus;
import com.justeat.backend.restaurant.repository.RestaurantRepository;
import com.justeat.backend.restaurant.service.RestaurantService;
import com.justeat.backend.common.enums.Role;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    private static final Logger log = LoggerFactory.getLogger(RestaurantServiceImpl.class);

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
                .status(restaurant.getStatus())
                .ownerName(restaurant.getOwner().getName())
                .ownerEmail(restaurant.getOwner().getEmail())
                .imageUrl(restaurant.getImageUrl())
                .build();
    }

    @Override
    @CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        User owner = getAuthenticatedUser();
        log.info("Creating restaurant '{}' for owner: {}", request.getName(), owner.getEmail());

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .cuisine(request.getCuisine())
                .rating(0.0)
                .imageUrl(request.getImageUrl())
                .owner(owner)
                .build();

        RestaurantResponse response = mapToResponse(restaurantRepository.save(restaurant));
        log.info("Restaurant created with id: {}", response.getId());
        return response;
    }

    @Override
    @CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        User owner = getAuthenticatedUser();
        log.info("Updating restaurant id: {} by owner: {}", id, owner.getEmail());

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            log.warn("Unauthorized update attempt on restaurant id: {} by user: {}", id, owner.getEmail());
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
        if (request.getImageUrl() != null) {
            restaurant.setImageUrl(request.getImageUrl());
        }

        return mapToResponse(restaurantRepository.save(restaurant));
    }

    @Override
    @CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
    public RestaurantResponse updateStatus(Long id, RestaurantStatusRequest request) {
        User currentUser = getAuthenticatedUser();

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = restaurant.getOwner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not authorized to update the status of this restaurant.");
        }

        RestaurantStatus newStatus = request.getStatus();

        // OWNER can only toggle between ACTIVE and INACTIVE
        if (!isAdmin) {
            if (newStatus == RestaurantStatus.SUSPENDED || newStatus == RestaurantStatus.CLOSED) {
                throw new AccessDeniedException(
                        "Only an ADMIN can set status to " + newStatus + ".");
            }
        }

        restaurant.setStatus(newStatus);
        return mapToResponse(restaurantRepository.save(restaurant));
    }

    @Override
    @CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
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
    @Cacheable(value = "restaurants")
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "restaurantSearch", key = "#name + '_' + #location + '_' + #cuisine")
    public List<RestaurantResponse> searchRestaurants(String name, String location, String cuisine) {
        return restaurantRepository.search(name, location, cuisine)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

}

