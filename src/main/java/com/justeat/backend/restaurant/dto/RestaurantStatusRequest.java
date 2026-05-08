package com.justeat.backend.restaurant.dto;

import com.justeat.backend.restaurant.enums.RestaurantStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for PATCH /restaurants/{id}/status
 * Contains only the status field to comply with REST partial-update semantics.
 *
 * Allowed transitions:
 *  - OWNER: ACTIVE ↔ INACTIVE
 *  - ADMIN: any status including SUSPENDED / CLOSED
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantStatusRequest {

    @NotNull(message = "Status must not be null")
    private RestaurantStatus status;
}

