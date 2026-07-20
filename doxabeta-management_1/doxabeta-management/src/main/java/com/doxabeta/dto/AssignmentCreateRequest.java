package com.doxabeta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Expected JSON body for POST /api/assignments. grade/feedback are set later via the grade endpoint, not here. */
public record AssignmentCreateRequest(
        @NotNull Long studentId,
        @NotBlank String title,
        String description
) {
}
