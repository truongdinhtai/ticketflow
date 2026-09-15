package com.ticketflow.booking.controller;

import com.ticketflow.booking.dto.AvailabilityResponse;
import com.ticketflow.booking.dto.BookingResponse;
import com.ticketflow.booking.dto.CreateBookingRequest;
import com.ticketflow.booking.service.AvailabilityService;
import com.ticketflow.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * The authenticated user's email arrives as the {@code X-User-Email} header,
 * set by the gateway after it verifies the JWT (a client cannot forge it — the
 * gateway overwrites any incoming value). Read operations are scoped to that
 * user so no one can list or fetch someone else's bookings.
 */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Create and look up ticket bookings")
public class BookingController {

    private static final String USER_HEADER = "X-User-Email";

    private final BookingService bookingService;
    private final AvailabilityService availabilityService;

    public BookingController(BookingService bookingService, AvailabilityService availabilityService) {
        this.bookingService = bookingService;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/availability/{eventId}")
    @Operation(summary = "Available seats for an event (Redis cache-aside)")
    public AvailabilityResponse availability(@PathVariable Long eventId) {
        return availabilityService.getAvailableSeats(eventId);
    }

    @PostMapping
    @Operation(summary = "Create a booking (reserves tickets from Event Service, publishes an event)")
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader(value = USER_HEADER, required = false) String userEmail,
            UriComponentsBuilder uriBuilder) {
        // Identity always comes from the authenticated user, never the body.
        BookingResponse created = bookingService.createBooking(request, requireUser(userEmail));
        URI location = uriBuilder.path("/api/bookings/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List the authenticated user's bookings (paged)")
    public Page<BookingResponse> list(@RequestHeader(value = USER_HEADER, required = false) String userEmail,
                                      Pageable pageable) {
        return bookingService.listForUser(requireUser(userEmail), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of the authenticated user's bookings by id")
    public BookingResponse getById(@PathVariable Long id,
                                   @RequestHeader(value = USER_HEADER, required = false) String userEmail) {
        return bookingService.getByIdForUser(id, requireUser(userEmail));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get one of the authenticated user's bookings by reference")
    public BookingResponse getByReference(@PathVariable String reference,
                                          @RequestHeader(value = USER_HEADER, required = false) String userEmail) {
        return bookingService.getByReferenceForUser(reference, requireUser(userEmail));
    }

    private String requireUser(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return userEmail;
    }
}
