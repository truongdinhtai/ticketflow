package com.ticketflow.event.batch;

import java.math.BigDecimal;

/**
 * A single aggregated row read by the batch job: total tickets and revenue for
 * one event on the target day. Mapped from a GROUP BY query over ticket_sales.
 */
public record DailyEventAggregate(
        Long eventId,
        String eventName,
        long ticketsSold,
        BigDecimal revenue
) {
}
