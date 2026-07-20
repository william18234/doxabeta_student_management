package com.doxabeta.exception;

import com.doxabeta.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Central error handler for the whole API.
 *
 * @RestControllerAdvice makes Spring apply the @ExceptionHandler methods below
 * to every @RestController in the app — so instead of each controller having
 * its own try/catch blocks, an exception thrown anywhere in a request (in a
 * controller, service, or repository call) bubbles up here automatically and
 * gets turned into a consistent {@link ErrorResponse} JSON body with the right
 * HTTP status code.
 *
 * Handlers are matched by exception type, most-specific first — Spring picks
 * the @ExceptionHandler whose declared type is the closest match to the
 * exception actually thrown.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** A lookup (e.g. GET /api/students/999) found nothing -> 404 Not Found. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /** A create request reused a unique code/name that's proactively checked in the service layer -> 409 Conflict. */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Catch-all for uniqueness/foreign-key violations the database itself
     * rejects (a safety net for anything DuplicateResourceException didn't
     * catch proactively — e.g. a race condition between two concurrent
     * requests, or a unique constraint that isn't explicitly checked in code).
     * -> 409 Conflict with a generic but honest message.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "The request could not be completed because it conflicts with existing data (duplicate code/email or invalid reference).",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Thrown automatically by Spring when a @Valid-annotated request body
     * fails one or more Jakarta Bean Validation constraints (e.g. a missing
     * @NotBlank field). Extracts each failing field + its message into a
     * "field: message" list so the caller knows exactly what to fix,
     * rather than just "validation failed". -> 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Business-rule failures that don't fit a Bean Validation annotation, e.g.
     * DailyHoursService rejecting a timeOut that isn't after timeIn.
     * -> 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Last-resort safety net for anything not handled above (e.g. an
     * unexpected NullPointerException, a checked exception from CSV parsing
     * during a re-seed, etc.). Ensures the API NEVER leaks a raw Java stack
     * trace to the caller — every error path returns the same ErrorResponse
     * JSON shape. -> 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
