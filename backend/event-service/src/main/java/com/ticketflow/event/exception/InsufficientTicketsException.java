package com.ticketflow.event.exception;

/**
 * Thrown when a reservation cannot be satisfied because the event is sold out,
 * lacks enough available tickets, or is no longer scheduled -> HTTP 409.
 */
public class InsufficientTicketsException extends RuntimeException {

    public InsufficientTicketsException(String message) {
        super(message);
    }
}
