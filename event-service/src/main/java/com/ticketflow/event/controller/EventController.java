package com.ticketflow.event.controller;

import com.ticketflow.event.dto.CreateEventRequest;
import com.ticketflow.event.dto.EventResponse;
import com.ticketflow.event.dto.ReservationRequest;
import com.ticketflow.event.dto.ReservationResponse;
import com.ticketflow.event.dto.UpdateEventRequest;
import com.ticketflow.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Manage events and reserve ticket inventory")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "List events (paged)")
    public Page<EventResponse> list(Pageable pageable) {
        return eventService.list(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one event by id")
    public EventResponse getById(@PathVariable Long id) {
        return eventService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new event")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request,
                                                UriComponentsBuilder uriBuilder) {
        EventResponse created = eventService.create(request);
        URI location = uriBuilder.path("/api/events/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event's details")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest request) {
        return eventService.update(id, request);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an event (no longer bookable)")
    public EventResponse cancel(@PathVariable Long id) {
        return eventService.cancel(id);
    }

    @PostMapping("/{id}/reservations")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reserve tickets (called by Booking Service). "
            + "Atomically decrements availability; 409 if sold out or not bookable.")
    public ReservationResponse reserve(@PathVariable Long id, @Valid @RequestBody ReservationRequest request) {
        return eventService.reserve(id, request.quantity());
    }
}
