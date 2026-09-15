package com.ticketflow.booking.exception;

/** Event Service refused the reservation (sold out / not bookable) -> HTTP 409. */
public class EventReservationRejectedException extends RuntimeException {
    public EventReservationRejectedException(String message) {
        super(message);
    }
}
