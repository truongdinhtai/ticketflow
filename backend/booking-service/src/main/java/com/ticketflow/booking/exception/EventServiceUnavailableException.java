package com.ticketflow.booking.exception;

/**
 * Event Service could not be reached or kept failing (connection refused,
 * timeouts, 5xx, or the circuit breaker is open) -> HTTP 503. Signals the
 * client to retry later. Raised by the Resilience4j fallback.
 */
public class EventServiceUnavailableException extends RuntimeException {
    public EventServiceUnavailableException(String message) {
        super(message);
    }
}
