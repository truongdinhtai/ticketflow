package com.ticketflow.event.exception;

/** Thrown when a requested entity (e.g. an event) does not exist -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException event(Long id) {
        return new ResourceNotFoundException("Event not found: " + id);
    }
}
