package com.ticketflow.event.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Request from Booking Service to reserve tickets on an event.
 */
public record ReservationRequest(

        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = 20, message = "quantity must not exceed 20 per booking")
        int quantity
) {
}
