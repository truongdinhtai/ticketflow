package com.ticketflow.common.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable Kafka event contract published by Booking Service and consumed by
 * Notification Service when a booking is confirmed.
 *
 * <p>This is the ONLY coupling between the two services. It is a plain data
 * carrier (a record) with no behaviour, so both sides can evolve independently
 * as long as this shape is respected. Treat fields as append-only for backward
 * compatibility.
 *
 * @param bookingId        internal id of the booking in Booking Service
 * @param bookingReference human-friendly reference shown to the customer
 * @param eventId          id of the event that was booked (owned by Event Service)
 * @param eventName        denormalised event name, so consumers need no extra call
 * @param customerName     name of the customer, for the notification body
 * @param customerEmail    where the notification would be sent
 * @param quantity         number of tickets booked
 * @param totalAmount      total charged amount
 * @param occurredAt       when the booking was confirmed (event time)
 */
public record BookingConfirmedEvent(
        Long bookingId,
        String bookingReference,
        Long eventId,
        String eventName,
        String customerName,
        String customerEmail,
        Integer quantity,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}
