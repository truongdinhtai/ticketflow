package com.ticketflow.booking.dto;

/** Available seats for an event, served (and cached) by Booking Service. */
public record AvailabilityResponse(Long eventId, Integer availableTickets) {
}
