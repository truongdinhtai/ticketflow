package com.ticketflow.event.repository;

import com.ticketflow.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Atomically reserve {@code quantity} tickets. The {@code WHERE} clause
     * enforces the invariant (enough tickets AND the event is still scheduled)
     * inside a single UPDATE, so concurrent reservations cannot oversell.
     *
     * @return number of rows updated: 1 if the reservation succeeded, 0 if it
     *         was rejected (sold out, insufficient tickets, or not scheduled)
     */
    @Modifying
    @Query("""
            UPDATE Event e
               SET e.availableTickets = e.availableTickets - :quantity
             WHERE e.id = :eventId
               AND e.status = com.ticketflow.event.domain.EventStatus.SCHEDULED
               AND e.availableTickets >= :quantity
            """)
    int reserveTickets(@Param("eventId") Long eventId, @Param("quantity") int quantity);
}
