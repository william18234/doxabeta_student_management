package com.doxabeta.dto;

import com.doxabeta.entity.Assignment;
import com.doxabeta.entity.AssignmentStatus;

import java.time.LocalDateTime;

/**
 * What an Assignment looks like over the API.
 */
public record AssignmentResponse(
        Long id,
        Long studentId,
        String studentName,
        String title,
        String description,
        LocalDateTime submittedAt,
        Integer grade,
        String feedback,
        AssignmentStatus status
) {
    /** Converts a JPA entity into this response shape. */
    public static AssignmentResponse from(Assignment a) {
        return new AssignmentResponse(
                a.getId(),
                a.getStudent() != null ? a.getStudent().getId() : null,
                a.getStudent() != null ? a.getStudent().getName() : null,
                a.getTitle(),
                a.getDescription(),
                a.getSubmittedAt(),
                a.getGrade(),
                a.getFeedback(),
                a.getStatus()
        );
    }
}
