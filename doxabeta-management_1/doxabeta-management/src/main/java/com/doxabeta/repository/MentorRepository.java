package com.doxabeta.repository;

import com.doxabeta.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data-access layer for {@link Mentor}. See StudentRepository's class comment
 * for how Spring Data JPA implements these methods automatically.
 */
public interface MentorRepository extends JpaRepository<Mentor, Long> {

    /** Looks up a mentor by their human-readable code (e.g. "MEN001"). Used by the CSV seeder. */
    Optional<Mentor> findByCode(String code);

    /** Used by MentorService.create(...) to reject duplicate mentor codes with a friendly 409 error. */
    boolean existsByCode(String code);
}
