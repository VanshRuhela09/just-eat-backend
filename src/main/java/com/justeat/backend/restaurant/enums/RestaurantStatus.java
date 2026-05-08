package com.justeat.backend.restaurant.enums;

public enum RestaurantStatus {
    /** Restaurant is open and accepting orders. Settable by OWNER or ADMIN. */
    ACTIVE,
    /** Restaurant is temporarily closed by owner. Settable by OWNER or ADMIN. */
    INACTIVE,
    /** Restaurant suspended by admin (e.g., policy violation). ADMIN-only. */
    SUSPENDED,
    /** Restaurant permanently closed. ADMIN-only. */
    CLOSED
}

