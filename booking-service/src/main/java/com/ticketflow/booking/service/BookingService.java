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

    public BookingService(EventReservationGateway eventReservationGateway,
                          BookingRepository bookingRepository,
                          BookingEventPublisher eventPublisher) {
        this.eventReservationGateway = eventReservationGateway;
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
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
    public BookingResponse createBooking(CreateBookingRequest request) {
        ReservationResponse reservation =
                eventReservationGateway.reserve(request.eventId(), request.quantity());

        Booking booking = persist(request, reservation);

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
    private Booking persist(CreateBookingRequest request, ReservationResponse reservation) {
        Booking booking = new Booking(
                generateReference(),
                reservation.eventId(),
                reservation.eventName(),
                request.customerName(),
                request.customerEmail(),
                request.quantity(),
                reservation.unitPrice(),
                reservation.totalAmount());
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> list(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(BookingResponse::from);
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return bookingRepository.findById(id)
                .map(BookingResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.booking(id));
    }

    @Transactional(readOnly = true)
    public BookingResponse getByReference(String reference) {
        return bookingRepository.findByBookingReference(reference)
                .map(BookingResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.booking(reference));
    }

    private String generateReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
