package com.doxabeta.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Expected JSON body for PUT /api/assignments/{id}/grade. Grade is required and constrained to 0-100. */
public record AssignmentGradeRequest(
        @NotNull @Min(0) @Max(100) Integer grade,
        String feedback
) {
}
