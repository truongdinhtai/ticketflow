package com.ticketflow.event.dto;

import com.ticketflow.event.domain.DailyEventSalesReport;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One line of a day's sales report. */
public record DailyReportLineResponse(
        LocalDate reportDate,
        Long eventId,
        String eventName,
        long ticketsSold,
        BigDecimal revenue
) {
    public static DailyReportLineResponse from(DailyEventSalesReport r) {
        return new DailyReportLineResponse(
                r.getReportDate(), r.getEventId(), r.getEventName(), r.getTicketsSold(), r.getRevenue());
    }
}
