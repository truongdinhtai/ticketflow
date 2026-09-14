package com.ticketflow.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Declarative REST client to Event Service. The {@code name} resolves to a
 * Eureka-registered instance and is load-balanced automatically. Errors are
 * translated by {@link EventServiceErrorDecoder}; resilience (retry + circuit
 * breaker) is applied by {@link EventReservationGateway}.
 */
@FeignClient(name = "event-service", configuration = EventClientConfig.class)
public interface EventServiceClient {

    @PostMapping("/api/events/{eventId}/reservations")
    ReservationResponse reserve(@PathVariable("eventId") Long eventId,
                                @RequestBody ReservationRequest request);
}
