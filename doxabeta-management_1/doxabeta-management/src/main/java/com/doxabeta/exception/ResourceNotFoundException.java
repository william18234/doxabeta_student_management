package com.doxabeta.exception;

/**
 * Thrown when a lookup by id (or other identifier) finds nothing — e.g. calling
 * GET /api/students/999 when no student with id 999 exists.
 *
 * This is a RuntimeException (unchecked), so service methods don't need a
 * `throws` clause and controllers don't need try/catch: it propagates straight
 * up to {@link GlobalExceptionHandler#handleNotFound}, which turns it into a
 * clean 404 JSON response instead of a stack trace.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience factory for the common "X not found with id: Y" case, e.g.
     * {@code ResourceNotFoundException.of("Student", 999)} ->
     * "Student not found with id: 999". Used throughout the service layer
     * wherever a repository.findById(...).orElseThrow(...) needs an exception.
     */
    public static ResourceNotFoundException of(String entityName, Object id) {
        return new ResourceNotFoundException(entityName + " not found with id: " + id);
    }
}
