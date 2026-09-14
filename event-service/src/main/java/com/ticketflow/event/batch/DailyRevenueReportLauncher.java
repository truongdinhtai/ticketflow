package com.ticketflow.event.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Launches the daily revenue report job. Shared by the scheduler (nightly run)
 * and the admin REST endpoint (on-demand run for a given date).
 */
@Component
public class DailyRevenueReportLauncher {

    private static final Logger log = LoggerFactory.getLogger(DailyRevenueReportLauncher.class);

    private final JobLauncher jobLauncher;
    private final Job dailyRevenueReportJob;

    public DailyRevenueReportLauncher(JobLauncher jobLauncher, Job dailyRevenueReportJob) {
        this.jobLauncher = jobLauncher;
        this.dailyRevenueReportJob = dailyRevenueReportJob;
    }

    /**
     * Run the report for a specific day. A unique {@code run.id} parameter makes
     * each launch a fresh job instance (so the same date can be re-run); the
     * job's purge step keeps the result idempotent.
     */
    public JobExecution launchFor(LocalDate reportDate) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("reportDate", reportDate.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        log.info("Launching {} for reportDate={}", DailyRevenueReportJobConfig.JOB_NAME, reportDate);
        JobExecution execution = jobLauncher.run(dailyRevenueReportJob, params);
        log.info("Job {} finished with status {}", DailyRevenueReportJobConfig.JOB_NAME, execution.getStatus());
        return execution;
    }
}
