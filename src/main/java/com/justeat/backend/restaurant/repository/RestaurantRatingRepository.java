package com.justeat.backend.restaurant.repository;

import com.justeat.backend.restaurant.entity.Restaurant;
import com.justeat.backend.restaurant.entity.RestaurantRating;
import com.justeat.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RestaurantRatingRepository extends JpaRepository<RestaurantRating, Long> {
    Optional<RestaurantRating> findByRestaurantAndUser(Restaurant restaurant, User user);

    @Query("SELECT AVG(r.rating) FROM RestaurantRating r WHERE r.restaurant = :restaurant")
    Double findAverageByRestaurant(@Param("restaurant") Restaurant restaurant);
}