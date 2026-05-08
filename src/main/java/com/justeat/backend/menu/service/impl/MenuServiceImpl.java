package com.justeat.backend.menu.service.impl;

import com.justeat.backend.menu.dto.MenuItemAvailabilityRequest;
import com.justeat.backend.menu.dto.MenuItemRequest;
import com.justeat.backend.menu.dto.MenuItemResponse;
import com.justeat.backend.menu.entity.MenuItem;
import com.justeat.backend.menu.repository.MenuItemRepository;
import com.justeat.backend.menu.service.MenuService;
import com.justeat.backend.restaurant.entity.Restaurant;
import com.justeat.backend.restaurant.repository.RestaurantRepository;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private static final Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;


    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + email));
    }


    private Restaurant getRestaurantAndValidateOwnership(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));

        User loggedInUser = getAuthenticatedUser();

        if (!restaurant.getOwner().getId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException("Unauthorized access: You do not own this restaurant.");
        }

        return restaurant;
    }


    private MenuItemResponse mapToResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .isAvailable(item.getIsAvailable())
                .isSpecial(item.getIsSpecial())
                .restaurantName(item.getRestaurant().getName())
                .imageUrl(item.getImageUrl())
                .build();
    }

    @Override
    @CacheEvict(value = {"menuByRestaurant", "menuSpecials"}, allEntries = true)
    public MenuItemResponse addMenuItem(MenuItemRequest request) {
        Restaurant restaurant = getRestaurantAndValidateOwnership(request.getRestaurantId());
        log.info("Adding menu item '{}' to restaurant id: {}", request.getName(), request.getRestaurantId());

        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .isAvailable(request.getIsAvailable())
                .isSpecial(request.getIsSpecial())
                .imageUrl(request.getImageUrl())
                .restaurant(restaurant)
                .build();

        MenuItemResponse response = mapToResponse(menuItemRepository.save(menuItem));
        log.info("Menu item created with id: {}", response.getId());
        return response;
    }

    @Override
    @CacheEvict(value = {"menuByRestaurant", "menuSpecials"}, allEntries = true)
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        log.info("Updating menu item id: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));

        getRestaurantAndValidateOwnership(menuItem.getRestaurant().getId());

        if (request.getName() != null && !request.getName().isBlank()) {
            menuItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }
        if (request.getPrice() != null && request.getPrice() > 0) {
            menuItem.setPrice(request.getPrice());
        }
        if (request.getIsAvailable() != null) {
            menuItem.setIsAvailable(request.getIsAvailable());
        }
        if (request.getIsSpecial() != null) {
            menuItem.setIsSpecial(request.getIsSpecial());
        }
        if (request.getImageUrl() != null) {
            menuItem.setImageUrl(request.getImageUrl());
        }

        return mapToResponse(menuItemRepository.save(menuItem));
    }

    @Override
    @CacheEvict(value = {"menuByRestaurant", "menuSpecials"}, allEntries = true)
    public MenuItemResponse updateAvailability(Long id, MenuItemAvailabilityRequest request) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));

        // Only the owner of the restaurant this item belongs to can toggle availability
        getRestaurantAndValidateOwnership(menuItem.getRestaurant().getId());

        menuItem.setIsAvailable(request.getIsAvailable());

        return mapToResponse(menuItemRepository.save(menuItem));
    }

    @Override
    @CacheEvict(value = {"menuByRestaurant", "menuSpecials"}, allEntries = true)
    public void deleteMenuItem(Long id) {
        log.info("Deleting menu item id: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));

        getRestaurantAndValidateOwnership(menuItem.getRestaurant().getId());

        menuItemRepository.delete(menuItem);
        log.info("Menu item id: {} deleted successfully", id);
    }

    @Override
    @Cacheable(value = "menuByRestaurant", key = "#restaurantId")
    public List<MenuItemResponse> getMenuByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RuntimeException("Restaurant not found with id: " + restaurantId);
        }

        return menuItemRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "menuSpecials")
    public List<MenuItemResponse> getAllSpecials() {
        return menuItemRepository.findByIsSpecialTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}

