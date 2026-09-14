package com.ticketflow.booking.client;

import java.math.BigDecimal;

/** Response from Event Service's reservation endpoint. */
public record ReservationResponse(
        Long eventId,
        String eventName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount
) {
}
