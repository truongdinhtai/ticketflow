package com.ticketflow.auth.dto;

/**
 * Returned on successful register/login. {@code token} is a signed JWT the
 * frontend stores and sends as {@code Authorization: Bearer <token>}.
 */
public record AuthResponse(
        String token,
        String email,
        String displayName,
        long expiresInMs
) {
}
