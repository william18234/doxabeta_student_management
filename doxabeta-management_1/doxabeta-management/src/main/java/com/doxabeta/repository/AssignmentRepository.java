package com.doxabeta.repository;

import com.doxabeta.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data-access layer for {@link Assignment}.
 */
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /** All assignments submitted by one student, most recently submitted first. */
    List<Assignment> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    /** All assignments across every student, most recently submitted first. */
    List<Assignment> findAllByOrderBySubmittedAtDesc();
}
