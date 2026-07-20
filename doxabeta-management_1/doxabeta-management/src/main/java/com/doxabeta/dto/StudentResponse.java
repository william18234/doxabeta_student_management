package com.doxabeta.dto;

import com.doxabeta.entity.Student;
import com.doxabeta.entity.StudentStatus;

/**
 * What a Student looks like over the API. This is a flattened view of the
 * {@link Student} entity: instead of nesting the full Cohort/Mentor objects
 * (which would require extra queries and risks lazy-loading errors), it just
 * pulls out the id + name of each and inlines them as cohortId/cohortName and
 * mentorId/mentorName. Simple, predictable JSON, no surprises for frontend code.
 *
 * Being a Java record, this class is immutable and gets equals()/hashCode()/
 * toString() for free — there's no need to hand-write a DTO class with getters.
 */
public record StudentResponse(
        Long id,
        String code,
        String name,
        String email,
        StudentStatus status,
        Long cohortId,
        String cohortName,
        Long mentorId,
        String mentorName
) {
    /**
     * Converts a JPA entity into this response shape.
     *
     * IMPORTANT: this must be called while the JPA session is still open (i.e.
     * from inside a @Transactional service method), because reading
     * s.getCohort()/s.getMentor() here triggers Hibernate to lazily fetch those
     * associations from the database if they haven't been loaded yet.
     */
    public static StudentResponse from(Student s) {
        return new StudentResponse(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getEmail(),
                s.getStatus(),
                s.getCohort() != null ? s.getCohort().getId() : null,
                s.getCohort() != null ? s.getCohort().getName() : null,
                s.getMentor() != null ? s.getMentor().getId() : null,
                s.getMentor() != null ? s.getMentor().getName() : null
        );
    }
}
