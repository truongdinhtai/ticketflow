package com.ticketflow.booking.controller;

import com.ticketflow.booking.dto.BookingResponse;
import com.ticketflow.booking.dto.CreateBookingRequest;
import com.ticketflow.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Create and look up ticket bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create a booking (reserves tickets from Event Service, publishes an event)")
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request,
                                                  UriComponentsBuilder uriBuilder) {
        BookingResponse created = bookingService.createBooking(request);
        URI location = uriBuilder.path("/api/bookings/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List bookings (paged)")
    public Page<BookingResponse> list(Pageable pageable) {
        return bookingService.list(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a booking by id")
    public BookingResponse getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get a booking by its reference")
    public BookingResponse getByReference(@PathVariable String reference) {
        return bookingService.getByReference(reference);
    }
}
