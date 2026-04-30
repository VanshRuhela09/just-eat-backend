package com.justeat.backend.menu.entity;

import com.justeat.backend.common.entity.BaseEntity;
import com.justeat.backend.restaurant.entity.Restaurant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isSpecial = false;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer orderCount = 0;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isPopular = false;

    private LocalDateTime lastPopularityUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}

