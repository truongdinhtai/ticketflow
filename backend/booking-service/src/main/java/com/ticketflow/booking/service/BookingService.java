package com.ticketflow.booking.service;

import com.ticketflow.booking.client.EventReservationGateway;
import com.ticketflow.booking.client.ReservationResponse;
import com.ticketflow.booking.domain.Booking;
import com.ticketflow.booking.dto.BookingResponse;
import com.ticketflow.booking.dto.CreateBookingRequest;
import com.ticketflow.booking.exception.ResourceNotFoundException;
import com.ticketflow.booking.messaging.BookingEventPublisher;
import com.ticketflow.booking.repository.BookingRepository;
import com.ticketflow.common.event.BookingConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final EventReservationGateway eventReservationGateway;
    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;
    private final AvailabilityService availabilityService;

    public BookingService(EventReservationGateway eventReservationGateway,
                          BookingRepository bookingRepository,
                          BookingEventPublisher eventPublisher,
                          AvailabilityService availabilityService) {
        this.eventReservationGateway = eventReservationGateway;
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
        this.availabilityService = availabilityService;
    }

    /**
     * Create a booking:
     * <ol>
     *   <li>reserve tickets in Event Service (sync REST, resilience-guarded) —
     *       done <b>outside</b> any DB transaction so a remote call never holds a
     *       connection open;</li>
     *   <li>persist the confirmed booking;</li>
     *   <li>publish a {@link BookingConfirmedEvent} to Kafka for Notification.</li>
     * </ol>
     *
     * <p>Note: if the reservation succeeds but persistence fails, tickets could
     * be left reserved with no booking. In a production system this would be
     * handled with a saga / compensating "release tickets" action; it is called
     * out here as a deliberate simplification.
     */
    public BookingResponse createBooking(CreateBookingRequest request, String customerEmail) {
        ReservationResponse reservation =
                eventReservationGateway.reserve(request.eventId(), request.quantity());

        Booking booking = persist(request, reservation, customerEmail);

        // Availability changed → invalidate the cached seat count for this event
        // so the next availability read repopulates with a fresh value.
        availabilityService.evict(request.eventId());

        eventPublisher.publishBookingConfirmed(new BookingConfirmedEvent(
                booking.getId(),
                booking.getBookingReference(),
                booking.getEventId(),
                booking.getEventName(),
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                booking.getQuantity(),
                booking.getTotalAmount(),
                Instant.now()));

        log.info("Created booking {} for event {} ({} ticket(s), total {})",
                booking.getBookingReference(), booking.getEventId(),
                booking.getQuantity(), booking.getTotalAmount());
        return BookingResponse.from(booking);
    }

    // Single-entity save is atomic via the repository's own transaction; no
    // class-level @Transactional here, so the remote reservation call above is
    // never made while holding a database connection.
    private Booking persist(CreateBookingRequest request, ReservationResponse reservation, String customerEmail) {
        Booking booking = new Booking(
                generateReference(),
                reservation.eventId(),
                reservation.eventName(),
                request.customerName(),
                customerEmail,           // authoritative identity (from the JWT via the gateway)
                request.quantity(),
                reservation.unitPrice(),
                reservation.totalAmount());
        return bookingRepository.save(booking);
    }

    /** Privacy: returns only the authenticated user's bookings. */
    @Transactional(readOnly = true)
    public Page<BookingResponse> listForUser(String customerEmail, Pageable pageable) {
        return bookingRepository.findByCustomerEmail(customerEmail, pageable).map(BookingResponse::from);
    }

    /** Owner-scoped lookup: a booking not owned by the caller is reported as not found. */
    @Transactional(readOnly = true)
    public BookingResponse getByIdForUser(Long id, String customerEmail) {
        return bookingRepository.findById(id)
                .filter(b -> b.getCustomerEmail().equals(customerEmail))
                .map(BookingResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.booking(id));
    }

    @Transactional(readOnly = true)
    public BookingResponse getByReferenceForUser(String reference, String customerEmail) {
        return bookingRepository.findByBookingReference(reference)
                .filter(b -> b.getCustomerEmail().equals(customerEmail))
                .map(BookingResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.booking(reference));
    }

    private String generateReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
