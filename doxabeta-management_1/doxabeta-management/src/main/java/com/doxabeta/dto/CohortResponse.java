package com.doxabeta.dto;

import com.doxabeta.entity.Cohort;

/**
 * What a Cohort looks like over the API. studentCount is computed by the
 * caller (see CohortService) rather than stored on the entity, so it's always
 * accurate and never needs to be kept in sync manually.
 */
public record CohortResponse(
        Long id,
        String name,
        long studentCount
) {
    /** Converts a JPA entity plus a separately-computed student count into this response shape. */
    public static CohortResponse from(Cohort c, long studentCount) {
        return new CohortResponse(c.getId(), c.getName(), studentCount);
    }
}
