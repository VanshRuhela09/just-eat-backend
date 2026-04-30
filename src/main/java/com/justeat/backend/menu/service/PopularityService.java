package com.justeat.backend.menu.service;

import com.justeat.backend.menu.config.PopularityConfig;
import com.justeat.backend.menu.entity.MenuItem;
import com.justeat.backend.menu.repository.MenuItemRepository;
import com.justeat.backend.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularityService {

    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final PopularityConfig popularityConfig;

    /**
     * Calculate and update popularity for all menu items across all restaurants.
     */
    @Transactional
    public void calculatePopularityForAllItems() {
        LocalDateTime since = LocalDateTime.now().minusDays(popularityConfig.getCalculationDays());

        // Get order counts for all menu items
        List<Object[]> orderCounts = orderItemRepository.getOrderCountsByMenuItem(since);

        if (orderCounts.isEmpty()) {
            log.info("No orders found in the last {} days", popularityConfig.getCalculationDays());
            return;
        }

        Map<Long, Long> menuItemOrderCounts = new HashMap<>();
        for (Object[] row : orderCounts) {
            Long menuItemId = (Long) row[0];
            Long count = ((Number) row[1]).longValue();
            menuItemOrderCounts.put(menuItemId, count);
        }

        // Update order counts in menu items
        for (Map.Entry<Long, Long> entry : menuItemOrderCounts.entrySet()) {
            menuItemRepository.updateOrderCount(entry.getKey(), entry.getValue().intValue());
        }

        // Calculate threshold for popularity (percentile-based)
        List<Long> sortedCounts = menuItemOrderCounts.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        int thresholdIndex = (int) (sortedCounts.size() * (100 - popularityConfig.getPercentileThreshold()) / 100);
        long popularityThreshold = sortedCounts.get(Math.min(thresholdIndex, sortedCounts.size() - 1));

        // Ensure minimum orders requirement
        popularityThreshold = Math.max(popularityThreshold, popularityConfig.getMinimumOrders());

        // Reset all popularity flags first
        List<MenuItem> allItems = menuItemRepository.findAll();
        for (MenuItem item : allItems) {
            menuItemRepository.updatePopularityStatus(item.getId(), false);
        }

        // Update popularity status for items meeting threshold
        int popularCount = 0;
        for (Map.Entry<Long, Long> entry : menuItemOrderCounts.entrySet()) {
            boolean isPopular = entry.getValue() >= popularityThreshold;
            if (isPopular) {
                menuItemRepository.updatePopularityStatus(entry.getKey(), true);
                popularCount++;
            }
        }

        log.info("Popularity calculation completed. {} items marked as popular out of {} total items. Threshold: {} orders",
                popularCount, menuItemOrderCounts.size(), popularityThreshold);
    }

    /**
     * Calculate popularity for a specific restaurant's menu items.
     */
    @Transactional
    public void calculatePopularityForRestaurant(Long restaurantId) {
        LocalDateTime since = LocalDateTime.now().minusDays(popularityConfig.getCalculationDays());

        // Reset popularity for this restaurant
        menuItemRepository.resetPopularityForRestaurant(restaurantId);

        // Get order counts for restaurant's items
        List<Object[]> orderCounts = orderItemRepository.getOrderCountsByRestaurant(restaurantId, since);

        if (orderCounts.isEmpty()) {
            log.info("No orders found for restaurant {} in the last {} days",
                    restaurantId, popularityConfig.getCalculationDays());
            return;
        }

        Map<Long, Long> menuItemOrderCounts = new LinkedHashMap<>();
        for (Object[] row : orderCounts) {
            Long menuItemId = (Long) row[0];
            Long count = ((Number) row[1]).longValue();
            menuItemOrderCounts.put(menuItemId, count);
        }

        // Update order counts
        for (Map.Entry<Long, Long> entry : menuItemOrderCounts.entrySet()) {
            menuItemRepository.updateOrderCount(entry.getKey(), entry.getValue().intValue());
        }

        // Calculate restaurant-specific threshold
        List<Long> sortedCounts = menuItemOrderCounts.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        int thresholdIndex = (int) (sortedCounts.size() * (100 - popularityConfig.getPercentileThreshold()) / 100);
        long popularityThreshold = sortedCounts.get(Math.min(thresholdIndex, sortedCounts.size() - 1));
        popularityThreshold = Math.max(popularityThreshold, popularityConfig.getMinimumOrders());

        // Update popularity status
        int popularCount = 0;
        for (Map.Entry<Long, Long> entry : menuItemOrderCounts.entrySet()) {
            boolean isPopular = entry.getValue() >= popularityThreshold;
            if (isPopular) {
                menuItemRepository.updatePopularityStatus(entry.getKey(), true);
                popularCount++;
            }
        }

        log.info("Popularity calculation completed for restaurant {}. {} items marked as popular. Threshold: {} orders",
                restaurantId, popularCount, popularityThreshold);
    }

    /**
     * Get popular items for a restaurant.
     */
    @Transactional(readOnly = true)
    public List<MenuItem> getPopularItemsForRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsPopularTrue(restaurantId);
    }

    /**
     * Get all popular items across all restaurants.
     */
    @Transactional(readOnly = true)
    public List<MenuItem> getAllPopularItems() {
        return menuItemRepository.findByIsPopularTrue();
    }

    /**
     * Get menu items sorted by order count (most ordered first).
     */
    @Transactional(readOnly = true)
    public List<MenuItem> getMostOrderedItemsForRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdOrderByOrderCountDesc(restaurantId);
    }

    /**
     * Increment order count when an order is placed.
     * Called when a new order is created.
     */
    @Transactional
    public void incrementOrderCount(Long menuItemId, Integer quantity) {
        menuItemRepository.incrementOrderCount(menuItemId, quantity);
    }
}

