package com.doxabeta.dto;

import com.doxabeta.entity.ReviewStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Expected JSON body for POST /api/reviews.
 * status is optional — if omitted, ReviewService.create(...) defaults it to
 * ReviewStatus.DRAFT rather than requiring every caller to specify it.
 */
public record ReviewCreateRequest(
        @NotNull Long studentId,
        @NotNull Long mentorId,
        @NotNull LocalDate reviewDate,
        @Min(1) @Max(5) Integer score,
        String learningOutcomes,
        String notes,
        String nextSteps,
        ReviewStatus status
) {
}
