package com.doxabeta.dto;

/**
 * Response shape for GET /api/admin/overview — a quick health/count snapshot
 * used for the admin dashboard view. All counts are computed live with
 * repository.count() calls (see AdminService.overview), so this is always
 * accurate as of the moment the request was made.
 */
public record AdminOverviewResponse(
        /** Always "UP" if this response was returned at all — the app being able to answer means it's healthy. */
        String status,
        long studentCount,
        long mentorCount,
        long cohortCount,
        long dailyHoursCount,
        long reviewCount,
        long assignmentCount
) {
}
