package com.justeat.backend.order.controller;

import com.justeat.backend.order.dto.OrderRequest;
import com.justeat.backend.order.dto.OrderResponse;
import com.justeat.backend.order.dto.UpdateOrderStatusRequest;
import com.justeat.backend.order.service.OrderService;
import com.justeat.backend.restaurant.dto.RatingRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /orders/place
     * Place an order from the cart.
     * Only accessible by CUSTOMER role.
     */
    @PostMapping("/place")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request));
    }

    /**
     * GET /orders/my
     * Get logged-in user's order history.
     * Only accessible by CUSTOMER role.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    /**
     * GET /orders/{id}
     * Get order details by ID.
     * Accessible by both CUSTOMER (own orders) and OWNER (their restaurant orders).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * GET /orders/restaurant
     * Get all orders for the owner's restaurant(s).
     * Only accessible by OWNER role.
     */
    @GetMapping("/restaurant")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrders() {
        return ResponseEntity.ok(orderService.getRestaurantOrders());
    }

    /**
     * PUT /orders/{id}/status
     * Update order status.
     * Only accessible by OWNER role.
     * Status flow: PENDING → PREPARING → READY → COMPLETED
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }

    /**
     * PATCH /orders/{id}/rate
     * Rate a completed order.
     * Only accessible by CUSTOMER role.
     */
    @PatchMapping("/{id}/rate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> rateOrder(
            @PathVariable Long id,
            @RequestBody RatingRequest ratingRequest) {
        return ResponseEntity.ok(orderService.rateOrder(id, ratingRequest.getRating()));
    }
}

