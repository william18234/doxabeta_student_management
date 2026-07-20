package com.doxabeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity mapped to the "cohorts" table. A cohort is simply a named group of
 * students (e.g. "2026-Spring"). Students are placed into a cohort via
 * PUT /api/students/{id}/cohort.
 */
@Entity
@Table(name = "cohorts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cohort {

    /** Auto-incrementing primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The cohort's display name, e.g. "2026". Unique so that CohortService and
     * the CSV seeder can find-or-create a cohort by name idempotently, and so
     * StudentRepository.search(...) can filter students by cohort name.
     */
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Inverse side of Student.cohort — see the equivalent field on {@link Mentor}
     * for why this is @JsonIgnore and read-only from Hibernate's perspective.
     * Callers use StudentRepository.findByCohortIdOrderById(...) instead
     * (see CohortService.getStudents).
     */
    @OneToMany(mappedBy = "cohort", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Student> students = new ArrayList<>();
}
