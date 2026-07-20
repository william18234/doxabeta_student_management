package com.doxabeta.exception;

/**
 * Thrown when a create request would violate a uniqueness rule the app checks
 * proactively — e.g. POST /api/students with a code that's already in use.
 * Caught by {@link GlobalExceptionHandler#handleDuplicate} and turned into a
 * 409 Conflict with a human-readable message, rather than a raw database
 * constraint-violation error.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
