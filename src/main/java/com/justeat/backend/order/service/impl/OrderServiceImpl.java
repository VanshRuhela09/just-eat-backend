package com.justeat.backend.order.service.impl;

import com.justeat.backend.cart.entity.Cart;
import com.justeat.backend.cart.entity.CartItem;
import com.justeat.backend.cart.repository.CartRepository;
import com.justeat.backend.menu.service.PopularityService;
import com.justeat.backend.order.dto.OrderRequest;
import com.justeat.backend.order.dto.OrderItemResponse;
import com.justeat.backend.order.dto.OrderResponse;
import com.justeat.backend.order.entity.Order;
import com.justeat.backend.order.entity.OrderItem;
import com.justeat.backend.order.enums.OrderStatus;
import com.justeat.backend.order.repository.OrderRepository;
import com.justeat.backend.order.service.OrderService;
import com.justeat.backend.restaurant.entity.Restaurant;
import com.justeat.backend.restaurant.entity.RestaurantRating;
import com.justeat.backend.restaurant.repository.RestaurantRatingRepository;
import com.justeat.backend.restaurant.repository.RestaurantRepository;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantRatingRepository restaurantRatingRepository;
    private final PopularityService popularityService;

    /**
     * Get the currently authenticated user from SecurityContextHolder.
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + email));
    }

    /**
     * Map Order entity to OrderResponse DTO.
     */
    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .name(item.getMenuItem().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .totalPrice(item.getPrice() * item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .customerName(order.getUser().getName())
                .customerEmail(order.getUser().getEmail())
                .restaurantName(order.getRestaurant().getName())
                .restaurantId(order.getRestaurant().getId())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getOrderCreatedAt())
                .rating(order.getRating())
                .addressLine(order.getAddressLine())
                .build();
    }

    /**
     * Validate status transition follows the correct flow:
     * PENDING → PREPARING → READY → COMPLETED
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PREPARING;
            case PREPARING -> newStatus == OrderStatus.READY;
            case READY -> newStatus == OrderStatus.COMPLETED;
            case COMPLETED -> false;
        };

        if (!isValid) {
            throw new IllegalArgumentException(
                    "Invalid status transition: Cannot change from " + currentStatus + " to " + newStatus);
        }
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        User user = getAuthenticatedUser();
        log.info("Placing order for user: {}", user.getEmail());

        // Fetch user's cart
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found. Please add items to cart first."));

        // Ensure cart is not empty
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Please add items to cart before placing an order.");
        }

        // Ensure all items belong to SAME restaurant
        Set<Long> restaurantIds = cart.getItems().stream()
                .map(item -> item.getMenuItem().getRestaurant().getId())
                .collect(Collectors.toSet());

        if (restaurantIds.size() > 1) {
            throw new RuntimeException("All items in cart must belong to the same restaurant. " +
                    "Please remove items from different restaurants.");
        }

        // Get the restaurant
        Long restaurantId = restaurantIds.iterator().next();
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // Calculate total amount
        Double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();

        // Create Order
        Order order = Order.builder()
                .user(user)
                .restaurant(restaurant)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .orderCreatedAt(LocalDateTime.now())
                .addressLine(request.getAddressLine())
                .build();

        // Convert each CartItem → OrderItem
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .menuItem(cartItem.getMenuItem())
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getMenuItem().getPrice()) // Store price at time of order
                        .order(order)
                        .build())
                .collect(Collectors.toList());

        order.setItems(orderItems);

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with id: {} for user: {}", savedOrder.getId(), user.getEmail());

        // Real-time popularity update: increment order count per item, then recalculate restaurant popularity
        for (OrderItem orderItem : savedOrder.getItems()) {
            popularityService.incrementOrderCountAndRecalculate(
                    orderItem.getMenuItem().getId(),
                    orderItem.getQuantity(),
                    restaurantId
            );
        }

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        User user = getAuthenticatedUser();
        return orderRepository.findByUserIdOrderByOrderCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        User user = getAuthenticatedUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Ensure the order belongs to the logged-in user OR the user is the restaurant owner
        boolean isCustomer = order.getUser().getId().equals(user.getId());
        boolean isOwner = order.getRestaurant().getOwner().getId().equals(user.getId());

        if (!isCustomer && !isOwner) {
            throw new AccessDeniedException("You are not authorized to view this order.");
        }

        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getRestaurantOrders() {
        User owner = getAuthenticatedUser();

        // Find restaurants owned by this user
        List<Restaurant> ownedRestaurants = restaurantRepository.findByOwnerId(owner.getId());

        if (ownedRestaurants.isEmpty()) {
            throw new RuntimeException("You don't own any restaurants.");
        }

        // Get orders for all owned restaurants
        return ownedRestaurants.stream()
                .flatMap(restaurant -> orderRepository
                        .findByRestaurantIdOrderByOrderCreatedAtDesc(restaurant.getId())
                        .stream())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        User owner = getAuthenticatedUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Ensure the logged-in user is the owner of the restaurant
        if (!order.getRestaurant().getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You are not authorized to update this order. " +
                    "Only the restaurant owner can update order status.");
        }

        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order id: {} status updated to: {}", orderId, newStatus);

        return mapToResponse(updatedOrder);
    }

    @Override
    public OrderResponse rateOrder(Long orderId, Double rating) {
        User user = getAuthenticatedUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Only the customer who placed the order can rate it
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to rate this order.");
        }

        // Only completed orders can be rated
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("You can only rate a completed order.");
        }

        // Validate rating value (1 to 5)
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        // Save rating on the order
        order.setRating(rating);
        orderRepository.save(order);

        // Save or update the RestaurantRating for this user-restaurant pair
        Restaurant restaurant = order.getRestaurant();
        RestaurantRating rr = restaurantRatingRepository.findByRestaurantAndUser(restaurant, user)
                .orElse(RestaurantRating.builder().restaurant(restaurant).user(user).build());
        rr.setRating(rating);
        restaurantRatingRepository.save(rr);

        // Recalculate and update the restaurant's average rating
        Double avg = restaurantRatingRepository.findAverageByRestaurant(restaurant);
        restaurant.setRating(avg);
        restaurantRepository.save(restaurant);

        return mapToResponse(order);
    }
}

