package com.doxabeta.dto;

import jakarta.validation.constraints.NotBlank;

/** Expected JSON body for POST /api/cohorts. */
public record CohortCreateRequest(
        @NotBlank String name
) {
}
