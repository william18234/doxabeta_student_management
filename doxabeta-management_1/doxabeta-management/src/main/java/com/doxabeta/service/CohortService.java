package com.doxabeta.service;

import com.doxabeta.dto.CohortCreateRequest;
import com.doxabeta.dto.CohortResponse;
import com.doxabeta.dto.StudentResponse;
import com.doxabeta.entity.Cohort;
import com.doxabeta.exception.DuplicateResourceException;
import com.doxabeta.exception.ResourceNotFoundException;
import com.doxabeta.repository.CohortRepository;
import com.doxabeta.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for cohorts: listing (with computed student counts),
 * creation, and fetching a cohort's students.
 */
@Service
@RequiredArgsConstructor
public class CohortService {

    private final CohortRepository cohortRepository;
    private final StudentRepository studentRepository;

    /**
     * Backs GET /api/cohorts. For each cohort, runs a second query to count
     * its students so the response can include studentCount without needing
     * a stored/denormalized counter on the Cohort entity (see the comment on
     * Cohort.students for why that collection isn't used directly).
     */
    @Transactional(readOnly = true)
    public List<CohortResponse> findAll() {
        return cohortRepository.findAll().stream()
                .map(c -> CohortResponse.from(c, studentRepository.findByCohortIdOrderById(c.getId()).size()))
                .toList();
    }

    /** Backs GET /api/cohorts/{id}/students. Validates the cohort exists first, for a clean 404 on a bad id. */
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudents(Long cohortId) {
        findCohortOrThrow(cohortId);
        return studentRepository.findByCohortIdOrderById(cohortId).stream()
                .map(StudentResponse::from)
                .toList();
    }

    /**
     * Backs POST /api/cohorts. Rejects a duplicate name with a 409.
     * A brand-new cohort obviously has zero students yet, so the response is
     * built with studentCount hardcoded to 0 rather than running a query that
     * would always return the same answer.
     */
    @Transactional
    public CohortResponse create(CohortCreateRequest request) {
        if (cohortRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("A cohort named '" + request.name() + "' already exists");
        }
        Cohort cohort = new Cohort();
        cohort.setName(request.name());
        Cohort saved = cohortRepository.save(cohort);
        return CohortResponse.from(saved, 0);
    }

    private Cohort findCohortOrThrow(Long id) {
        return cohortRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Cohort", id));
    }
}
