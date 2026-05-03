package com.justeat.backend.restaurant.entity;

import com.justeat.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"restaurant_id", "user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RestaurantRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double rating;
}
