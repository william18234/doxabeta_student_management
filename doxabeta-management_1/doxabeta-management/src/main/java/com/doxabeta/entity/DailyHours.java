package com.doxabeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * JPA entity mapped to the "daily_hours" table — one row per day a student
 * clocks time in/out (e.g. for placement/internship hour tracking).
 *
 * The hours-worked figure isn't stored; it's calculated on the fly from
 * timeIn/timeOut when converting to {@link com.doxabeta.dto.DailyHoursResponse}
 * (see DailyHoursResponse.from), so it can never drift out of sync with the
 * raw times.
 */
@Entity
@Table(name = "daily_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyHours {

    /** Auto-incrementing primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The student this log entry belongs to. Required (optional = false) —
     * every daily-hours row must reference a real student.
     * @JsonIgnoreProperties({"mentor", "cohort"}) stops Jackson from trying to
     * walk further into the student's own lazy associations if this entity were
     * ever serialized directly.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"mentor", "cohort"})
    private Student student;

    /** Calendar date the hours were logged for. */
    @NotNull
    private LocalDate date;

    /** Clock-in time. */
    @NotNull
    private LocalTime timeIn;

    /** Clock-out time. DailyHoursService validates this is after timeIn before saving. */
    @NotNull
    private LocalTime timeOut;

    /** Optional free-text notes about the day's work. */
    @Column(length = 1000)
    private String notes;
}
