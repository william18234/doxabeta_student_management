package com.doxabeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * JPA entity mapped to the "reviews" table — a mentor's periodic evaluation of
 * a student's progress (score, learning outcomes, notes, next steps).
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    /** Auto-incrementing primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The student being reviewed. Required. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"mentor", "cohort"})
    private Student student;

    /** The mentor who wrote the review. Required. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id", nullable = false)
    @JsonIgnoreProperties({"students"})
    private Mentor mentor;

    /** Date the review was conducted/written. */
    @NotNull
    private LocalDate reviewDate;

    /** Numeric rating from 1 (lowest) to 5 (highest). Optional — a review can omit a score. */
    @Min(1)
    @Max(5)
    private Integer score;

    /** Free-text summary of what the student learned/accomplished. */
    @Column(length = 2000)
    private String learningOutcomes;

    /** Free-text general notes from the mentor. */
    @Column(length = 2000)
    private String notes;

    /** Free-text plan for what the student should focus on next. */
    @Column(length = 2000)
    private String nextSteps;

    /** Draft vs published state; see {@link ReviewStatus}. Defaults to DRAFT. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.DRAFT;
}
