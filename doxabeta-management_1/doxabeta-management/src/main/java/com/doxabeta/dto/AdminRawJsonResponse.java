package com.doxabeta.dto;

import java.util.List;

/**
 * Response shape for GET /api/admin/raw-json — dumps every record in the
 * system as one JSON payload. Intended for debugging/demo purposes (e.g.
 * quickly inspecting all seeded data), not for production-scale use, since it
 * loads every table into memory at once with no pagination.
 */
public record AdminRawJsonResponse(
        List<StudentResponse> students,
        List<MentorResponse> mentors,
        List<CohortResponse> cohorts,
        List<DailyHoursResponse> dailyHours,
        List<ReviewResponse> reviews,
        List<AssignmentResponse> assignments
) {
}
