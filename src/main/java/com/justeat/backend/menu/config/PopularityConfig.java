package com.justeat.backend.menu.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "popularity")
@Getter
@Setter
public class PopularityConfig {

    /**
     * Minimum number of orders required for an item to be considered popular.
     */
    private int minimumOrders = 10;

    /**
     * Percentile threshold - items in top X% are marked popular.
     * Default 75.0 means top 25% items are popular.
     */
    private double percentileThreshold = 75.0;

    /**
     * Number of days to consider for calculating popularity.
     */
    private int calculationDays = 30;
}

