package com.ticketflow.auth.exception;

/** Wrong email/password on login -> HTTP 401. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
