package com.justeat.backend.menu.scheduler;

import com.justeat.backend.menu.service.PopularityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopularityScheduler {

    private final PopularityService popularityService;

    /**
     * Run popularity calculation every day at 2 AM.
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "${popularity.cron:0 0 2 * * *}")
    public void calculatePopularity() {
        log.info("Starting scheduled popularity calculation...");
        try {
            popularityService.calculatePopularityForAllItems();
            log.info("Completed scheduled popularity calculation successfully");
        } catch (Exception e) {
            log.error("Error during scheduled popularity calculation", e);
        }
    }
}

