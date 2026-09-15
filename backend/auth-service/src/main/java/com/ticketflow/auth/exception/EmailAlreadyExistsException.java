package com.ticketflow.auth.exception;

/** Registration with an email that is already taken -> HTTP 409. */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
