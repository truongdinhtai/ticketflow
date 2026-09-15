package com.ticketflow.booking.dto;

import com.ticketflow.booking.domain.Booking;
import com.ticketflow.booking.domain.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** API view of a {@link Booking}. */
public record BookingResponse(
        Long id,
        String bookingReference,
        Long eventId,
        String eventName,
        String customerName,
        String customerEmail,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        BookingStatus status,
        Instant createdAt
) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getBookingReference(),
                b.getEventId(),
                b.getEventName(),
                b.getCustomerName(),
                b.getCustomerEmail(),
                b.getQuantity(),
                b.getUnitPrice(),
                b.getTotalAmount(),
                b.getStatus(),
                b.getCreatedAt());
    }
}
