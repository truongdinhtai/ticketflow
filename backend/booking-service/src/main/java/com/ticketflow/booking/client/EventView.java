package com.ticketflow.booking.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Partial view of an event as returned by Event Service's GET /api/events/{id}.
 * Only the fields Booking Service needs; other JSON fields are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventView(Long id, String name, Integer availableTickets) {
}
