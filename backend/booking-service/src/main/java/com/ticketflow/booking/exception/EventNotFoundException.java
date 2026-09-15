package com.ticketflow.booking.exception;

/** The referenced event does not exist in Event Service -> HTTP 404. */
public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message) {
        super(message);
    }
}
