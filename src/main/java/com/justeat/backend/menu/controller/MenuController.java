package com.justeat.backend.menu.controller;

import com.justeat.backend.menu.dto.MenuItemAvailabilityRequest;
import com.justeat.backend.menu.dto.MenuItemRequest;
import com.justeat.backend.menu.dto.MenuItemResponse;
import com.justeat.backend.menu.service.MenuService;
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
public class MenuController {

    private final MenuService menuService;


    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<MenuItemResponse> addMenuItem(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.addMenuItem(request));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.updateMenuItem(id, request));
    }

    /**
     * PATCH /menu/{id}/availability
     * Partially updates only the isAvailable flag of a menu item.
     * Only the OWNER of the restaurant this item belongs to can call this.
     */
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<MenuItemResponse> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemAvailabilityRequest request) {
        return ResponseEntity.ok(menuService.updateAvailability(id, request));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<String> deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok("Menu item deleted successfully.");
    }


    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemResponse>> getMenuByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getMenuByRestaurant(restaurantId));
    }


    @GetMapping("/specials")
    public ResponseEntity<List<MenuItemResponse>> getAllSpecials() {
        return ResponseEntity.ok(menuService.getAllSpecials());
    }
}

