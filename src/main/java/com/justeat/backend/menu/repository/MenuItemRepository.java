package com.justeat.backend.menu.repository;

import com.justeat.backend.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByIsSpecialTrue();

    List<MenuItem> findByIsPopularTrue();

    List<MenuItem> findByIsPopularTrueOrderByOrderCountDesc();

    List<MenuItem> findByRestaurantIdAndIsPopularTrue(Long restaurantId);

    List<MenuItem> findByRestaurantIdOrderByOrderCountDesc(Long restaurantId);

    @Modifying
    @Query("UPDATE MenuItem m SET m.orderCount = :count WHERE m.id = :menuItemId")
    void updateOrderCount(@Param("menuItemId") Long menuItemId, @Param("count") Integer count);

    @Modifying
    @Query("UPDATE MenuItem m SET m.isPopular = :isPopular WHERE m.id = :menuItemId")
    void updatePopularityStatus(@Param("menuItemId") Long menuItemId, @Param("isPopular") Boolean isPopular);

    @Modifying
    @Query("UPDATE MenuItem m SET m.isPopular = false WHERE m.restaurant.id = :restaurantId")
    void resetPopularityForRestaurant(@Param("restaurantId") Long restaurantId);

    @Modifying
    @Query("UPDATE MenuItem m SET m.orderCount = m.orderCount + :quantity WHERE m.id = :menuItemId")
    void incrementOrderCount(@Param("menuItemId") Long menuItemId, @Param("quantity") Integer quantity);
}

