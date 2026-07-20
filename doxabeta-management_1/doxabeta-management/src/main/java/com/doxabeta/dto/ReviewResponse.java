package com.doxabeta.dto;

import com.doxabeta.entity.Review;
import com.doxabeta.entity.ReviewStatus;

import java.time.LocalDate;

/**
 * What a Review looks like over the API — flattens both the student and mentor
 * associations down to id + name, same approach as StudentResponse.
 */
public record ReviewResponse(
        Long id,
        Long studentId,
        String studentName,
        Long mentorId,
        String mentorName,
        LocalDate reviewDate,
        Integer score,
        String learningOutcomes,
        String notes,
        String nextSteps,
        ReviewStatus status
) {
    /** Converts a JPA entity into this response shape. Must run inside an open JPA session (see StudentResponse.from). */
    public static ReviewResponse from(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getStudent() != null ? r.getStudent().getId() : null,
                r.getStudent() != null ? r.getStudent().getName() : null,
                r.getMentor() != null ? r.getMentor().getId() : null,
                r.getMentor() != null ? r.getMentor().getName() : null,
                r.getReviewDate(),
                r.getScore(),
                r.getLearningOutcomes(),
                r.getNotes(),
                r.getNextSteps(),
                r.getStatus()
        );
    }
}
