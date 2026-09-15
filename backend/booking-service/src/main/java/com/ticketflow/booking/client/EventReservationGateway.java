package com.ticketflow.booking.client;

import com.ticketflow.booking.exception.EventNotFoundException;
import com.ticketflow.booking.exception.EventReservationRejectedException;
import com.ticketflow.booking.exception.EventServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Resilience boundary around the Event Service reservation call.
 *
 * <p>{@code @Retry} re-attempts transient failures (connection refused, 5xx);
 * {@code @CircuitBreaker} trips after repeated failures so we stop hammering a
 * down dependency and fail fast. Business errors (404/409) are configured as
 * ignored, so they are neither retried nor counted as circuit failures — they
 * propagate straight through. When the call ultimately fails for infrastructure
 * reasons, {@link #reserveFallback} converts it into a clean 503.
 *
 * <p>The instance name {@code "eventService"} ties these annotations to the
 * Resilience4j configuration in {@code config-repo/booking-service.yml}.
 */
@Component
public class EventReservationGateway {

    private static final Logger log = LoggerFactory.getLogger(EventReservationGateway.class);

    private final EventServiceClient eventServiceClient;

    public EventReservationGateway(EventServiceClient eventServiceClient) {
        this.eventServiceClient = eventServiceClient;
    }

    @Retry(name = "eventService")
    @CircuitBreaker(name = "eventService", fallbackMethod = "reserveFallback")
    public ReservationResponse reserve(Long eventId, int quantity) {
        log.debug("Calling Event Service to reserve {} ticket(s) for event {}", quantity, eventId);
        return eventServiceClient.reserve(eventId, new ReservationRequest(quantity));
    }

    /**
     * Fallback invoked by the circuit breaker. Business exceptions are rethrown
     * unchanged so the client still gets an accurate 404/409; anything else is a
     * degraded-dependency situation, surfaced as 503.
     */
    @SuppressWarnings("unused")
    private ReservationResponse reserveFallback(Long eventId, int quantity, Throwable t) {
        if (t instanceof EventNotFoundException e) {
            throw e;
        }
        if (t instanceof EventReservationRejectedException e) {
            throw e;
        }
        log.warn("Event Service reservation failed for event {} ({}): {}",
                eventId, t.getClass().getSimpleName(), t.getMessage());
        throw new EventServiceUnavailableException(
                "Event Service is currently unavailable. Please try again shortly.");
    }
}
