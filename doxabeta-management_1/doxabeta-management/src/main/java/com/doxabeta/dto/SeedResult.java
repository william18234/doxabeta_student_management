package com.doxabeta.dto;

/**
 * Summary of what happened during a CSV seed run — how many rows were read
 * from students.csv, and how many cohorts/mentors/students were newly created
 * vs. updated because they already existed. Returned by POST /api/admin/seed
 * so a caller can confirm the seed actually did something (or safely did
 * nothing, if run twice with the same data — see SeedService for the
 * idempotency logic).
 */
public record SeedResult(
        int rowsProcessed,
        int cohortsCreated,
        int mentorsCreated,
        int mentorsUpdated,
        int studentsCreated,
        int studentsUpdated
) {
}
