package com.justeat.backend.menu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for PATCH /menu/{id}/availability
 * Contains only the isAvailable flag — partial update semantics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemAvailabilityRequest {

    @NotNull(message = "isAvailable must not be null")
    private Boolean isAvailable;
}

