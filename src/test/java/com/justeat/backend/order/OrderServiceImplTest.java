package com.justeat.backend.order;

import com.justeat.backend.cart.entity.Cart;
import com.justeat.backend.cart.entity.CartItem;
import com.justeat.backend.cart.repository.CartRepository;
import com.justeat.backend.common.enums.Role;
import com.justeat.backend.menu.entity.MenuItem;
import com.justeat.backend.menu.service.PopularityService;
import com.justeat.backend.order.dto.OrderRequest;
import com.justeat.backend.order.dto.OrderResponse;
import com.justeat.backend.order.entity.Order;
import com.justeat.backend.order.enums.OrderStatus;
import com.justeat.backend.order.repository.OrderRepository;
import com.justeat.backend.order.service.impl.OrderServiceImpl;
import com.justeat.backend.restaurant.entity.Restaurant;
import com.justeat.backend.restaurant.repository.RestaurantRatingRepository;
import com.justeat.backend.restaurant.repository.RestaurantRepository;
import com.justeat.backend.user.entity.User;
import com.justeat.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private RestaurantRatingRepository restaurantRatingRepository;
    @Mock private PopularityService popularityService;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User customer;
    private Restaurant restaurant;
    private Cart cart;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(1L)
                .name("Customer")
                .email("customer@example.com")
                .role(Role.CUSTOMER)
                .build();

        restaurant = Restaurant.builder()
                .id(5L)
                .name("Burger Town")
                .owner(User.builder().id(99L).build())
                .build();

        menuItem = MenuItem.builder()
                .id(10L)
                .name("Burger")
                .price(9.99)
                .isAvailable(true)
                .restaurant(restaurant)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("customer@example.com");
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(customer));
    }

    @Test
    void placeOrder_emptyCart_throwsException() {
        cart = Cart.builder()
                .id(1L)
                .user(customer)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        OrderRequest request = new OrderRequest();
        request.setAddressLine("123 Main Street");

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void placeOrder_success() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .menuItem(menuItem)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(customer)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();
        cartItem.setCart(cart);

        Order savedOrder = Order.builder()
                .id(100L)
                .user(customer)
                .restaurant(restaurant)
                .totalAmount(19.98)
                .status(OrderStatus.PENDING)
                .addressLine("123 Main Street")
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(restaurantRepository.findById(5L)).thenReturn(Optional.of(restaurant));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderRequest request = new OrderRequest();
        request.setAddressLine("123 Main Street");

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(100L);
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(cart); // cart cleared after order
    }

    @Test
    void placeOrder_cartNotFound_throwsException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        OrderRequest request = new OrderRequest();
        request.setAddressLine("123 Main Street");

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cart not found");
    }

    @Test
    void getMyOrders_returnsOrderList() {
        Order order = Order.builder()
                .id(1L)
                .user(customer)
                .restaurant(restaurant)
                .totalAmount(19.98)
                .status(OrderStatus.PENDING)
                .addressLine("123 Main Street")
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findByUserIdOrderByOrderCreatedAtDesc(1L))
                .thenReturn(List.of(order));

        List<OrderResponse> responses = orderService.getMyOrders();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getOrderId()).isEqualTo(1L);
    }
}

