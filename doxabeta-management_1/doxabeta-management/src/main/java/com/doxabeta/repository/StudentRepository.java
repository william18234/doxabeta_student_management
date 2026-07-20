package com.doxabeta.repository;

import com.doxabeta.entity.Student;
import com.doxabeta.entity.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Data-access layer for {@link Student}. Extending JpaRepository gives us
 * findAll(), findById(), save(), deleteById(), count(), etc. for free — the
 * methods below are the extra queries this app needs beyond those basics.
 *
 * Spring Data JPA generates the implementation of every method here at runtime
 * from the method name / @Query annotation; there is no hand-written implementation
 * class anywhere in the project.
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

    /** Looks up a student by their human-readable code (e.g. "STU001"). Used by the CSV seeder. */
    Optional<Student> findByCode(String code);

    /** Cheaper than findByCode(...).isPresent() when you only need a yes/no answer (e.g. duplicate checks). */
    boolean existsByCode(String code);

    /**
     * Powers GET /api/students with optional mentorId / cohort / status filters.
     * Each filter is applied only if its parameter is non-null — passing null for
     * all three parameters returns every student. This "(:param IS NULL OR ...)"
     * pattern is a standard way to build one query that supports several optional
     * filters without constructing JPQL strings dynamically at runtime.
     *
     * s.mentor.id and s.cohort.name are "nested property" paths: JPQL follows the
     * @ManyToOne associations on Student to reach the id/name column on the
     * related Mentor/Cohort table, generating the appropriate SQL join.
     */
    @Query("SELECT s FROM Student s WHERE " +
           "(:mentorId IS NULL OR s.mentor.id = :mentorId) AND " +
           "(:cohort IS NULL OR s.cohort.name = :cohort) AND " +
           "(:status IS NULL OR s.status = :status) " +
           "ORDER BY s.id")
    List<Student> search(@Param("mentorId") Long mentorId,
                          @Param("cohort") String cohort,
                          @Param("status") StudentStatus status);

    /**
     * All students assigned to a given mentor, ordered by id.
     * "MentorId" in the method name is parsed by Spring Data as the nested path
     * mentor.id (Student has a `mentor` field, which itself has an `id` field) —
     * no @Query annotation is needed for a query this simple.
     * Used by GET /api/mentors/{id}/students.
     */
    List<Student> findByMentorIdOrderById(Long mentorId);

    /**
     * All students in a given cohort, ordered by id. Same derived-query pattern
     * as findByMentorIdOrderById above. Used by GET /api/cohorts/{id}/students.
     */
    List<Student> findByCohortIdOrderById(Long cohortId);
}
