package com.ticketflow.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Payload to create a booking. */
public record CreateBookingRequest(

        @NotNull(message = "eventId is required")
        Long eventId,

        @NotBlank(message = "customerName is required")
        String customerName,

        @NotBlank(message = "customerEmail is required")
        @Email(message = "customerEmail must be a valid email")
        String customerEmail,

        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = 20, message = "quantity must not exceed 20 per booking")
        int quantity
) {
}
