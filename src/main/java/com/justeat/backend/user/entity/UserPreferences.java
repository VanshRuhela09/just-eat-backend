package com.justeat.backend.user.entity;

import com.justeat.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ElementCollection
    @CollectionTable(name = "favourite_restaurants", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "restaurant_id")
    @Builder.Default
    private Set<Long> favouriteRestaurantIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "favourite_cuisines", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "cuisine")
    @Builder.Default
    private Set<String> favouriteCuisines = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "dietary_restrictions", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "restriction")
    @Builder.Default
    private Set<String> dietaryRestrictions = new HashSet<>();
}

