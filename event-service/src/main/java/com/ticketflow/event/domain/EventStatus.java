package com.ticketflow.event.domain;

/**
 * Lifecycle state of an event. Only {@link #SCHEDULED} events accept ticket
 * reservations.
 */
public enum EventStatus {
    SCHEDULED,
    CANCELLED,
    COMPLETED
}
