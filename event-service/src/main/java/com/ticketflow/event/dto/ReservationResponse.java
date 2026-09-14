package com.ticketflow.event.dto;

import java.math.BigDecimal;

/**
 * Result of a successful reservation. Booking Service uses these denormalised
 * fields (eventName, totalAmount) to build the booking and the Kafka event
 * without an extra call back to Event Service.
 */
public record ReservationResponse(
        Long eventId,
        String eventName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount
) {
}
