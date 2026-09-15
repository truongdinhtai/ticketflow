package com.ticketflow.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload to create a booking. The customer's email is NOT taken from here — it
 * comes from the authenticated identity ({@code X-User-Email}, set by the
 * gateway from the JWT) so a caller cannot book on someone else's behalf.
 */
public record CreateBookingRequest(

        @NotNull(message = "eventId is required")
        Long eventId,

        @NotBlank(message = "customerName is required")
        String customerName,

        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = 20, message = "quantity must not exceed 20 per booking")
        int quantity
) {
}
