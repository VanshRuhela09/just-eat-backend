package com.justeat.backend.menu.controller;

import com.justeat.backend.menu.dto.MenuItemAvailabilityRequest;
import com.justeat.backend.menu.dto.MenuItemRequest;
import com.justeat.backend.menu.dto.MenuItemResponse;
import com.justeat.backend.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Menu item management")
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "Add a menu item to a restaurant (OWNER only)")
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<MenuItemResponse> addMenuItem(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.addMenuItem(request));
    }

    @Operation(summary = "Update a menu item by ID (OWNER only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @Parameter(description = "Menu item ID") @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.updateMenuItem(id, request));
    }

    @Operation(summary = "Toggle availability of a menu item (OWNER only)")
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<MenuItemResponse> updateAvailability(
            @Parameter(description = "Menu item ID") @PathVariable Long id,
            @Valid @RequestBody MenuItemAvailabilityRequest request) {
        return ResponseEntity.ok(menuService.updateAvailability(id, request));
    }

    @Operation(summary = "Delete a menu item (OWNER only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<String> deleteMenuItem(@Parameter(description = "Menu item ID") @PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok("Menu item deleted successfully.");
    }

    @Operation(summary = "Get all menu items for a restaurant")
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemResponse>> getMenuByRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getMenuByRestaurant(restaurantId));
    }

    @Operation(summary = "Get all special menu items across all restaurants")
    @GetMapping("/specials")
    public ResponseEntity<List<MenuItemResponse>> getAllSpecials() {
        return ResponseEntity.ok(menuService.getAllSpecials());
    }
}
