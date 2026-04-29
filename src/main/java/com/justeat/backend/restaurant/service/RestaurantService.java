package com.justeat.backend.restaurant.service;

import com.justeat.backend.restaurant.dto.RestaurantRequest;
import com.justeat.backend.restaurant.dto.RestaurantResponse;

import java.util.List;

public interface RestaurantService {
    RestaurantResponse createRestaurant(RestaurantRequest request);
    RestaurantResponse updateRestaurant(Long id, RestaurantRequest request);
    void deleteRestaurant(Long id);
    List<RestaurantResponse> getAllRestaurants();
    List<RestaurantResponse> searchRestaurants(String name, String location, String cuisine);
}

