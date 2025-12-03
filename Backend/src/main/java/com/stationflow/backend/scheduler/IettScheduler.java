package com.stationflow.backend.scheduler;

import com.stationflow.backend.service.IettApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IettScheduler {

    @Autowired
    private IettApiService iettApiService;

    // Run monthly: at 00:00 on day-of-month 1
    @Scheduled(cron = "0 0 0 1 * ?")
    public void scheduleTask() {
        iettApiService.fetchAndSaveStations();
    }

    // Run on startup
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        iettApiService.fetchAndSaveStations();
    }
}
