package com.justeat.backend.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuItemRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.1", message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;

    @NotNull(message = "Special status is required")
    private Boolean isSpecial;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
}

