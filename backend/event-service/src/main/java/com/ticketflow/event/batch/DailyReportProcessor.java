package com.ticketflow.event.batch;

import com.ticketflow.event.domain.DailyEventSalesReport;
import org.springframework.batch.item.ItemProcessor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Maps an aggregated sales row into a report entity, stamping the report date
 * and generation time.
 */
public class DailyReportProcessor implements ItemProcessor<DailyEventAggregate, DailyEventSalesReport> {

    private final LocalDate reportDate;

    public DailyReportProcessor(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    @Override
    public DailyEventSalesReport process(DailyEventAggregate item) {
        return new DailyEventSalesReport(
                reportDate,
                item.eventId(),
                item.eventName(),
                item.ticketsSold(),
                item.revenue(),
                Instant.now());
    }
}
