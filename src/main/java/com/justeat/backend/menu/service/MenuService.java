package com.justeat.backend.menu.service;

import com.justeat.backend.menu.dto.MenuItemRequest;
import com.justeat.backend.menu.dto.MenuItemResponse;

import java.util.List;

public interface MenuService {
    MenuItemResponse addMenuItem(MenuItemRequest request);
    MenuItemResponse updateMenuItem(Long id, MenuItemRequest request);
    void deleteMenuItem(Long id);
    List<MenuItemResponse> getMenuByRestaurant(Long restaurantId);
    List<MenuItemResponse> getAllSpecials();
}

