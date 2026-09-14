package com.ticketflow.event.service;

import com.ticketflow.event.domain.Event;
import com.ticketflow.event.domain.EventStatus;
import com.ticketflow.event.domain.TicketSale;
import com.ticketflow.event.dto.CreateEventRequest;
import com.ticketflow.event.dto.EventResponse;
import com.ticketflow.event.dto.ReservationResponse;
import com.ticketflow.event.dto.UpdateEventRequest;
import com.ticketflow.event.exception.InsufficientTicketsException;
import com.ticketflow.event.exception.ResourceNotFoundException;
import com.ticketflow.event.repository.EventRepository;
import com.ticketflow.event.repository.TicketSaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final TicketSaleRepository ticketSaleRepository;

    public EventService(EventRepository eventRepository, TicketSaleRepository ticketSaleRepository) {
        this.eventRepository = eventRepository;
        this.ticketSaleRepository = ticketSaleRepository;
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> list(Pageable pageable) {
        return eventRepository.findAll(pageable).map(EventResponse::from);
    }

    @Transactional(readOnly = true)
    public EventResponse getById(Long id) {
        return EventResponse.from(findOrThrow(id));
    }

    @Transactional
    public EventResponse create(CreateEventRequest req) {
        Event event = new Event(req.name(), req.description(), req.venue(), req.city(),
                req.eventDateTime(), req.totalTickets(), req.ticketPrice());
        Event saved = eventRepository.save(event);
        log.info("Created event id={} name='{}' tickets={}", saved.getId(), saved.getName(), saved.getTotalTickets());
        return EventResponse.from(saved);
    }

    @Transactional
    public EventResponse update(Long id, UpdateEventRequest req) {
        Event event = findOrThrow(id);
        event.setName(req.name());
        event.setDescription(req.description());
        event.setVenue(req.venue());
        event.setCity(req.city());
        event.setEventDateTime(req.eventDateTime());
        event.setTicketPrice(req.ticketPrice());
        return EventResponse.from(event); // managed entity flushed on commit
    }

    @Transactional
    public EventResponse cancel(Long id) {
        Event event = findOrThrow(id);
        event.setStatus(EventStatus.CANCELLED);
        log.info("Cancelled event id={}", id);
        return EventResponse.from(event);
    }

    /**
     * Reserve tickets on behalf of Booking Service. The decrement is performed
     * by an atomic conditional UPDATE so concurrent reservations cannot
     * oversell; on success a {@link TicketSale} row is recorded for the daily
     * revenue report.
     */
    @Transactional
    public ReservationResponse reserve(Long eventId, int quantity) {
        Event event = findOrThrow(eventId);

        int updated = eventRepository.reserveTickets(eventId, quantity);
        if (updated == 0) {
            if (event.getStatus() != EventStatus.SCHEDULED) {
                throw new InsufficientTicketsException(
                        "Event %d is not open for booking (status=%s)".formatted(eventId, event.getStatus()));
            }
            throw new InsufficientTicketsException(
                    "Not enough tickets for event %d (requested %d, available %d)"
                            .formatted(eventId, quantity, event.getAvailableTickets()));
        }

        BigDecimal unitPrice = event.getTicketPrice();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        ticketSaleRepository.save(new TicketSale(eventId, quantity, unitPrice, totalAmount));

        log.info("Reserved {} ticket(s) for event id={} totalAmount={}", quantity, eventId, totalAmount);
        return new ReservationResponse(eventId, event.getName(), quantity, unitPrice, totalAmount);
    }

    private Event findOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.event(id));
    }
}
