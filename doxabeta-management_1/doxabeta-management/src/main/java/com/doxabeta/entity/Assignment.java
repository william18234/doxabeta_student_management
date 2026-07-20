package com.doxabeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity mapped to the "assignments" table — a piece of work a student
 * submits, which a mentor/admin can later grade.
 *
 * Lifecycle: created via POST /api/assignments (status = SUBMITTED, grade/feedback
 * null), then updated via PUT /api/assignments/{id}/grade (status becomes GRADED,
 * grade + feedback populated). See {@link com.doxabeta.service.AssignmentService}.
 */
@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    /** Auto-incrementing primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The student who submitted this assignment. Required. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"mentor", "cohort"})
    private Student student;

    /** Short title of the assignment, e.g. "Week 3 API Integration Exercise". */
    @NotBlank
    @Column(nullable = false)
    private String title;

    /** Longer free-text description of what was submitted / what was required. */
    @Column(length = 2000)
    private String description;

    /**
     * Timestamp of submission. Defaults to "now" at object-construction time, but
     * AssignmentService.create() explicitly overwrites this with the actual
     * request-handling time to avoid relying on class-loading-time defaults.
     */
    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    /** Numeric grade (0-100), null until a mentor/admin grades the assignment. */
    private Integer grade;

    /** Mentor/admin's written feedback, set at the same time as the grade. */
    @Column(length = 2000)
    private String feedback;

    /** SUBMITTED until graded, then GRADED. See {@link AssignmentStatus}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.SUBMITTED;
}
