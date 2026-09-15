package com.ticketflow.booking.service;

import com.ticketflow.booking.client.EventServiceClient;
import com.ticketflow.booking.client.EventView;
import com.ticketflow.booking.dto.AvailabilityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Serves an event's available-seats count with a **cache-aside** strategy
 * (Spring Cache over Redis):
 * <ul>
 *   <li>{@link #getAvailableSeats(Long)} — on a cache miss, reads from Event
 *       Service (Feign) and stores the result; subsequent reads hit Redis.</li>
 *   <li>{@link #evict(Long)} — called after a booking changes availability, so
 *       the next read repopulates with a fresh value.</li>
 * </ul>
 *
 * <p>Caching is toggled by {@code spring.cache.type} (redis / none), which is
 * how the before/after load test is run without code changes.
 */
@Service
public class AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityService.class);
    public static final String CACHE = "availableSeats";

    private final EventServiceClient eventServiceClient;

    public AvailabilityService(EventServiceClient eventServiceClient) {
        this.eventServiceClient = eventServiceClient;
    }

    @Cacheable(cacheNames = CACHE, key = "#eventId")
    public AvailabilityResponse getAvailableSeats(Long eventId) {
        // Only runs on a cache miss.
        log.debug("Cache miss — fetching availability for event {} from Event Service", eventId);
        EventView event = eventServiceClient.getEvent(eventId);
        return new AvailabilityResponse(eventId, event.availableTickets());
    }

    @CacheEvict(cacheNames = CACHE, key = "#eventId")
    public void evict(Long eventId) {
        log.debug("Evicted availability cache for event {}", eventId);
    }
}
