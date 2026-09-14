package com.ticketflow.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payload to create a new event. All inventory starts fully available.
 */
public record CreateEventRequest(

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

        @Min(value = 1, message = "totalTickets must be at least 1")
        int totalTickets,

        @NotNull(message = "ticketPrice is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "ticketPrice must be >= 0")
        BigDecimal ticketPrice
) {
}
