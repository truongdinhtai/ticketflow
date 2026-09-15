package com.ticketflow.event.dto;

import com.ticketflow.event.domain.Event;
import com.ticketflow.event.domain.EventStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * API view of an {@link Event}. Keeps the JPA entity out of the web layer.
 */
public record EventResponse(
        Long id,
        String name,
        String description,
        String venue,
        String city,
        Instant eventDateTime,
        int totalTickets,
        int availableTickets,
        BigDecimal ticketPrice,
        EventStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static EventResponse from(Event e) {
        return new EventResponse(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getVenue(),
                e.getCity(),
                e.getEventDateTime(),
                e.getTotalTickets(),
                e.getAvailableTickets(),
                e.getTicketPrice(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
