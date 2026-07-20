package com.doxabeta.repository;

import com.doxabeta.entity.Cohort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data-access layer for {@link Cohort}.
 */
public interface CohortRepository extends JpaRepository<Cohort, Long> {

    /**
     * Looks up a cohort by its unique display name (e.g. "2026"). Used both by
     * the CSV seeder (find-or-create) and by PUT /api/students/{id}/cohort,
     * which also creates the cohort on the fly if it doesn't exist yet.
     */
    Optional<Cohort> findByName(String name);

    /** Used by CohortService.create(...) to reject duplicate cohort names with a friendly 409 error. */
    boolean existsByName(String name);
}
