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

    // ─────────────────────────────────────────────────────────────
    // GLOBAL POPULARITY (platform-wide trending)
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns menu items flagged as globally popular, ordered by order count DESC.
     */
    @Transactional(readOnly = true)
    public List<MenuItem> getGlobalPopularItems() {
        return menuItemRepository.findByIsPopularTrueOrderByOrderCountDesc();
    }

    /**
     * Recalculates global popularity across ALL restaurants.
     * Groups by menuItemId, sums quantity, applies minimumOrders + percentile rules.
     * Used by cron scheduler as background reconciliation.
     */
    @Transactional
    public void calculateGlobalPopularity() {
        LocalDateTime since = LocalDateTime.now().minusDays(popularityConfig.getCalculationDays());

        List<Object[]> orderCounts = orderItemRepository.getOrderCountsByMenuItemAcrossAllRestaurants(since);

        if (orderCounts.isEmpty()) {
            log.info("[Global] No orders found in the last {} days", popularityConfig.getCalculationDays());
            return;
        }

        Map<Long, Long> menuItemOrderCounts = new LinkedHashMap<>();
        for (Object[] row : orderCounts) {
            Long menuItemId = (Long) row[0];
            Long count = ((Number) row[1]).longValue();
            menuItemOrderCounts.put(menuItemId, count);
        }

        // Update stored order counts
        menuItemOrderCounts.forEach((id, count) ->
                menuItemRepository.updateOrderCount(id, count.intValue()));

        // Calculate percentile threshold
        List<Long> sortedCounts = menuItemOrderCounts.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        int thresholdIndex = (int) (sortedCounts.size() * (100 - popularityConfig.getPercentileThreshold()) / 100);
        long popularityThreshold = sortedCounts.get(Math.min(thresholdIndex, sortedCounts.size() - 1));
        popularityThreshold = Math.max(popularityThreshold, popularityConfig.getMinimumOrders());

        // Reset all popularity flags globally
        menuItemRepository.findAll().forEach(item ->
                menuItemRepository.updatePopularityStatus(item.getId(), false));

        // Mark items that cross threshold
        final long finalThreshold = popularityThreshold;
        long popularCount = menuItemOrderCounts.entrySet().stream()
                .filter(e -> e.getValue() >= finalThreshold)
                .peek(e -> menuItemRepository.updatePopularityStatus(e.getKey(), true))
                .count();

        log.info("[Global] Popularity recalculated. {}/{} items marked popular. Threshold: {} orders",
                popularCount, menuItemOrderCounts.size(), finalThreshold);
    }

    // ─────────────────────────────────────────────────────────────
    // RESTAURANT-LEVEL POPULARITY
    // ─────────────────────────────────────────────────────────────

    /**
     * Calculate popularity for a specific restaurant's menu items only.
     * Called real-time after order placement and by scheduler.
     */
    @Transactional
    public void calculatePopularityForRestaurant(Long restaurantId) {
        LocalDateTime since = LocalDateTime.now().minusDays(popularityConfig.getCalculationDays());

        // Reset popularity for this restaurant only
        menuItemRepository.resetPopularityForRestaurant(restaurantId);

        List<Object[]> orderCounts = orderItemRepository.getOrderCountsByRestaurant(restaurantId, since);

        if (orderCounts.isEmpty()) {
            log.info("[Restaurant {}] No orders found in the last {} days", restaurantId, popularityConfig.getCalculationDays());
            return;
        }

        Map<Long, Long> menuItemOrderCounts = new LinkedHashMap<>();
        for (Object[] row : orderCounts) {
            Long menuItemId = (Long) row[0];
            Long count = ((Number) row[1]).longValue();
            menuItemOrderCounts.put(menuItemId, count);
        }

        // Update stored order counts
        menuItemOrderCounts.forEach((id, count) ->
                menuItemRepository.updateOrderCount(id, count.intValue()));

        // Calculate restaurant-specific percentile threshold
        List<Long> sortedCounts = menuItemOrderCounts.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        int thresholdIndex = (int) (sortedCounts.size() * (100 - popularityConfig.getPercentileThreshold()) / 100);
        long popularityThreshold = sortedCounts.get(Math.min(thresholdIndex, sortedCounts.size() - 1));
        popularityThreshold = Math.max(popularityThreshold, popularityConfig.getMinimumOrders());

        final long finalThreshold = popularityThreshold;
        long popularCount = menuItemOrderCounts.entrySet().stream()
                .filter(e -> e.getValue() >= finalThreshold)
                .peek(e -> menuItemRepository.updatePopularityStatus(e.getKey(), true))
                .count();

        log.info("[Restaurant {}] Popularity recalculated. {}/{} items marked popular. Threshold: {} orders",
                restaurantId, popularCount, menuItemOrderCounts.size(), finalThreshold);
    }

    /**
     * Get popular items for a specific restaurant.
     */
    @Transactional(readOnly = true)
    public List<MenuItem> getPopularItemsForRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsPopularTrue(restaurantId);
    }

    // ─────────────────────────────────────────────────────────────
    // REAL-TIME UPDATE (called from OrderService on order placement)
    // ─────────────────────────────────────────────────────────────

    /**
     * Increments orderCount for a menu item immediately when an order is placed.
     * After incrementing, triggers real-time restaurant-level popularity recalculation.
     *
     * @param menuItemId  the ordered item
     * @param quantity    quantity ordered
     * @param restaurantId the restaurant, used to re-evaluate popularity threshold
     */
    @Transactional
    public void incrementOrderCountAndRecalculate(Long menuItemId, Integer quantity, Long restaurantId) {
        menuItemRepository.incrementOrderCount(menuItemId, quantity);
        log.debug("[RealTime] Incremented orderCount for item {} by {}. Triggering restaurant {} recalculation.",
                menuItemId, quantity, restaurantId);
        calculatePopularityForRestaurant(restaurantId);
    }

    // ─────────────────────────────────────────────────────────────
    // LEGACY / SCHEDULER SUPPORT
    // ─────────────────────────────────────────────────────────────

    /**
     * Full recalculation across all items — used only by background scheduler.
     * @deprecated Prefer calculateGlobalPopularity() for global scope.
     */
    @Deprecated(since = "2.0", forRemoval = false)
    @Transactional
    public void calculatePopularityForAllItems() {
        log.info("[Scheduler] Running full global popularity reconciliation...");
        calculateGlobalPopularity();
    }
}
