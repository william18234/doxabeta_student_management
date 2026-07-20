package com.doxabeta.dto;

import com.doxabeta.entity.StudentStatus;
import jakarta.validation.constraints.Email;

/**
 * Expected JSON body for PUT /api/students/{id}.
 *
 * Every field here is optional (nothing is @NotBlank/@NotNull) because this is
 * a PARTIAL update: StudentService.update(...) only overwrites a field on the
 * existing student if the caller actually sent a value for it. Sending
 * {"name": "New Name"} alone, for example, leaves code/email/status/cohort/
 * mentor untouched.
 */
public record StudentUpdateRequest(
        String code,
        String name,
        @Email String email,
        StudentStatus status,
        Long cohortId,
        Long mentorId
) {
}
