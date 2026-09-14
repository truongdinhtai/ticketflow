package com.ticketflow.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard error body returned by every service's {@code @RestControllerAdvice}.
 * Having one shape across all services gives API consumers (and the gateway) a
 * predictable contract for failures.
 *
 * @param timestamp        when the error was produced
 * @param status           HTTP status code, e.g. 400
 * @param error            HTTP reason phrase, e.g. "Bad Request"
 * @param message          human-readable, safe-to-expose summary
 * @param path             request path that produced the error
 * @param validationErrors per-field validation messages; omitted when empty
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> validationErrors
) {

    /** A single field-level Bean Validation failure. */
    public record FieldValidationError(String field, String message) {
    }

    /** Convenience factory for the common case with no field-level errors. */
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, List.of());
    }
}
