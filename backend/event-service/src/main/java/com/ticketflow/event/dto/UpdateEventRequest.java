package com.ticketflow.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payload to update an event's descriptive details and price. Ticket capacity
 * is intentionally not editable here to keep inventory accounting simple;
 * cancelling is a separate operation.
 */
public record UpdateEventRequest(

        @NotBlank(message = "name is required")
        @Size(max = 255)
        String name,

        @Size(max = 2000)
        String description,

        @NotBlank(message = "venue is required")
        String venue,

        @NotBlank(message = "city is required")
        String city,

        @NotNull(message = "eventDateTime is required")
        @Future(message = "eventDateTime must be in the future")
        Instant eventDateTime,

        @NotNull(message = "ticketPrice is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "ticketPrice must be >= 0")
        BigDecimal ticketPrice
) {
}
