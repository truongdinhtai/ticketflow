package com.ticketflow.event.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Fires the daily revenue report once a day for the previous calendar day.
 * The cron expression is overridable via config ({@code ticketflow.report.cron}).
 */
@Component
public class DailyRevenueReportScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyRevenueReportScheduler.class);

    private final DailyRevenueReportLauncher launcher;

    public DailyRevenueReportScheduler(DailyRevenueReportLauncher launcher) {
        this.launcher = launcher;
    }

    // Default: every day at 01:00. Overridable via config for demos/tests.
    @Scheduled(cron = "${ticketflow.report.cron:0 0 1 * * *}", zone = "UTC")
    public void runDailyReport() {
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        try {
            launcher.launchFor(yesterday);
        } catch (Exception e) {
            log.error("Scheduled daily revenue report failed for {}", yesterday, e);
        }
    }
}
