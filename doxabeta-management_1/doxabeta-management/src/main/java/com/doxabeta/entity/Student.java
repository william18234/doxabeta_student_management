package com.doxabeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity mapped to the "students" table. This is the central record in the
 * system: every daily-hours log, review, and assignment points back to a Student.
 *
 * NOTE ON JSON SERIALIZATION: this entity is intentionally never returned directly
 * from a controller. It's converted to {@link com.doxabeta.dto.StudentResponse}
 * inside the service layer instead. That's deliberate, for two reasons:
 *   1. spring.jpa.open-in-view is set to false (see application.yml), so lazy
 *      fields like `cohort`/`mentor` are only safe to read while the database
 *      session is still open, i.e. inside a @Transactional service method.
 *   2. Serializing a raw Hibernate entity can leak internal proxy fields and
 *      cause infinite recursion (Student -> Mentor -> Student -> ...). Flattening
 *      to a DTO avoids both problems entirely.
 * The @JsonIgnoreProperties annotations below are a belt-and-suspenders safety net
 * in case an entity ever does get serialized directly (e.g. in a future endpoint).
 */
@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    /** Auto-incrementing primary key, assigned by the database on insert. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-assigned, unique student identifier (e.g. "STU001"), distinct from
     * the database id. This is what the CSV seed file and API callers use to
     * refer to a student in a stable way that doesn't depend on database order.
     */
    @NotBlank
    @Column(nullable = false, unique = true)
    private String code;

    /** Full display name. */
    @NotBlank
    @Column(nullable = false)
    private String name;

    /** Contact email. Validated for format but not required to be unique. */
    @Email
    private String email;

    /** Current program status; see {@link StudentStatus}. Defaults to ACTIVE for new students. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status = StudentStatus.ACTIVE;

    /**
     * The cohort this student belongs to. Nullable — a student can exist without
     * a cohort assignment (e.g. right after being created, before being placed).
     * FetchType.LAZY means Hibernate only queries the cohorts table when this
     * field is actually accessed, not automatically whenever a Student is loaded.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id")
    @JsonIgnoreProperties({"students"})
    private Cohort cohort;

    /**
     * The mentor assigned to this student. Nullable for the same reason as cohort
     * above — assignment happens via PUT /api/students/{id}/mentor/{mentorId}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    @JsonIgnoreProperties({"students"})
    private Mentor mentor;
}
