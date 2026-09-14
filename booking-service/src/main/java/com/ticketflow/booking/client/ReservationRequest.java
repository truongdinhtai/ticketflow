package com.ticketflow.booking.client;

/** Body sent to Event Service's reservation endpoint. */
public record ReservationRequest(int quantity) {
}
