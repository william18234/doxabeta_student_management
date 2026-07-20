package com.doxabeta.dto;

import com.doxabeta.entity.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Expected JSON body for POST /api/students.
 *
 * The @NotBlank/@Email annotations are Jakarta Bean Validation constraints.
 * Because the controller method parameter is annotated with @Valid
 * (see StudentController.create), Spring automatically validates an incoming
 * request against these constraints before the method body ever runs, and
 * returns a 400 with field-level error details if validation fails (handled by
 * GlobalExceptionHandler.handleValidation).
 *
 * cohortId/mentorId are optional (no @NotNull) — a student can be created
 * without being placed into a cohort or assigned a mentor yet.
 */
public record StudentCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        @Email String email,
        StudentStatus status,
        Long cohortId,
        Long mentorId
) {
}
