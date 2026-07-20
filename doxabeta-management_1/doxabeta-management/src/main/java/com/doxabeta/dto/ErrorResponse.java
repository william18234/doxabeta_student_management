package com.doxabeta.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Uniform JSON error body returned by every error path in the API (404s, 409s,
 * 400s, 500s) — see GlobalExceptionHandler, which is the only place this class
 * is constructed. Keeping one consistent shape means frontend error-handling
 * code only has to deal with one format, regardless of what went wrong.
 *
 * Two secondary constructors are provided purely for convenience, so callers
 * in GlobalExceptionHandler don't have to pass LocalDateTime.now() or a null
 * `details` list explicitly every time:
 *   - 4-arg constructor: for errors with no field-level details (404, 409, 500)
 *   - 5-arg constructor: for validation errors, which include a details list
 *     of "field: message" strings (see GlobalExceptionHandler.handleValidation)
 * Both simply delegate to the full 6-arg canonical constructor that Java
 * generates automatically for every record.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    /** Convenience constructor for errors without field-level validation details. */
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    /** Convenience constructor for errors that include field-level validation details. */
    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}
