package com.ticketflow.booking.client;

import com.ticketflow.booking.exception.EventNotFoundException;
import com.ticketflow.booking.exception.EventReservationRejectedException;
import feign.Response;
import feign.codec.ErrorDecoder;

/**
 * Translates Event Service HTTP errors into typed exceptions so the rest of the
 * service (and Resilience4j) can treat them correctly:
 * <ul>
 *   <li>404 -> {@link EventNotFoundException} (business error, not retried)</li>
 *   <li>409 -> {@link EventReservationRejectedException} (sold out / not bookable, not retried)</li>
 *   <li>everything else (incl. 5xx) -> the default {@code FeignException}, which
 *       Resilience4j is configured to retry / count as a circuit-breaker failure.</li>
 * </ul>
 */
public class EventServiceErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new EventNotFoundException("Event not found in Event Service");
            case 409 -> new EventReservationRejectedException(
                    "Reservation rejected by Event Service (sold out or not bookable)");
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}
