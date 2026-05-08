package com.justeat.backend.menu.scheduler;

import com.justeat.backend.menu.service.PopularityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background reconciliation scheduler for popularity.
 * Primary popularity updates are real-time (via OrderService).
 * This scheduler acts as a safety net / drift correction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PopularityScheduler {

    private final PopularityService popularityService;

    /**
     * Global popularity reconciliation — runs every hour.
     * Aggregates order counts across ALL restaurants and applies platform-wide threshold.
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "${popularity.global.cron:0 0 * * * *}")
    public void calculateGlobalPopularity() {
        log.info("[Scheduler] Starting global popularity reconciliation...");
        try {
            popularityService.calculateGlobalPopularity();
            log.info("[Scheduler] Completed global popularity reconciliation successfully");
        } catch (Exception e) {
            log.error("[Scheduler] Error during global popularity reconciliation", e);
        }
    }

    /**
     * Full nightly reconciliation — runs at 2 AM daily as a deep consistency check.
     */
    @Scheduled(cron = "${popularity.nightly.cron:0 0 2 * * *}")
    public void nightlyReconciliation() {
        log.info("[Scheduler] Starting nightly popularity deep reconciliation...");
        try {
            popularityService.calculatePopularityForAllItems();
            log.info("[Scheduler] Completed nightly popularity reconciliation successfully");
        } catch (Exception e) {
            log.error("[Scheduler] Error during nightly popularity reconciliation", e);
        }
    }
}
