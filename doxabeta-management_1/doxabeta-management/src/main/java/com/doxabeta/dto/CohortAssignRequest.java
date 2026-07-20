package com.doxabeta.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Expected JSON body for PUT /api/students/{id}/cohort, e.g. {"cohort": "2026"}.
 * Takes a cohort NAME rather than an id — if no cohort with that name exists
 * yet, StudentService.assignCohort(...) creates one on the fly.
 */
public record CohortAssignRequest(
        @NotBlank String cohort
) {
}
