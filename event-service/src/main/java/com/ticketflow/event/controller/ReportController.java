package com.ticketflow.event.controller;

import com.ticketflow.event.batch.DailyRevenueReportLauncher;
import com.ticketflow.event.dto.DailyReportLineResponse;
import com.ticketflow.event.repository.DailyEventSalesReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.batch.core.JobExecution;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * Admin endpoints for the daily revenue report: trigger the Spring Batch job
 * on demand (handy for demos/tests) and read a day's results.
 */
@RestController
@RequestMapping("/api/events/reports")
@Tag(name = "Reports", description = "Daily revenue report (Spring Batch)")
public class ReportController {

    private final DailyRevenueReportLauncher launcher;
    private final DailyEventSalesReportRepository reportRepository;

    public ReportController(DailyRevenueReportLauncher launcher,
                            DailyEventSalesReportRepository reportRepository) {
        this.launcher = launcher;
        this.reportRepository = reportRepository;
    }

    @PostMapping("/daily/run")
    @Operation(summary = "Trigger the daily revenue batch job for a date (defaults to today, UTC)")
    public ResponseEntity<Map<String, Object>> run(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws Exception {

        LocalDate target = (date != null) ? date : LocalDate.now(ZoneOffset.UTC);
        JobExecution execution = launcher.launchFor(target);
        return ResponseEntity.accepted().body(Map.of(
                "reportDate", target.toString(),
                "jobStatus", execution.getStatus().toString(),
                "exitCode", execution.getExitStatus().getExitCode()));
    }

    @GetMapping("/daily")
    @Operation(summary = "Read the daily sales report for a date (defaults to today, UTC)")
    public List<DailyReportLineResponse> daily(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate target = (date != null) ? date : LocalDate.now(ZoneOffset.UTC);
        return reportRepository.findByReportDateOrderByRevenueDesc(target).stream()
                .map(DailyReportLineResponse::from)
                .toList();
    }
}
