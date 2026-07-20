package com.doxabeta.service;

import com.doxabeta.dto.StudentCreateRequest;
import com.doxabeta.dto.StudentResponse;
import com.doxabeta.dto.StudentUpdateRequest;
import com.doxabeta.entity.Cohort;
import com.doxabeta.entity.Mentor;
import com.doxabeta.entity.Student;
import com.doxabeta.entity.StudentStatus;
import com.doxabeta.exception.DuplicateResourceException;
import com.doxabeta.exception.ResourceNotFoundException;
import com.doxabeta.repository.CohortRepository;
import com.doxabeta.repository.MentorRepository;
import com.doxabeta.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for students: search/lookup, create, update, and the two
 * "assign" operations (mentor, cohort) that back the dedicated PUT endpoints.
 *
 * Every public method here is @Transactional. That matters because
 * spring.jpa.open-in-view is false (see application.yml) — meaning the JPA
 * session normally closes as soon as the repository call returns. Wrapping
 * each method in a transaction keeps the session open for the method's full
 * duration, so it's safe to read lazy fields like student.getCohort() (via
 * StudentResponse.from) before the method returns its DTO.
 *
 * @RequiredArgsConstructor (Lombok) generates a constructor taking all three
 * `final` fields below, which Spring uses for constructor injection — no
 * @Autowired annotation needed since there's only one constructor.
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final CohortRepository cohortRepository;

    /**
     * Backs GET /api/students. Any/all of mentorId, cohort, status may be null,
     * in which case that filter is skipped (see StudentRepository.search for
     * how the optional-filter query works).
     * readOnly = true is a hint to Hibernate that lets it skip some
     * bookkeeping (dirty-checking) it would otherwise do to detect changes to
     * save — a small optimization for queries that never write.
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> search(Long mentorId, String cohort, StudentStatus status) {
        return studentRepository.search(mentorId, cohort, status).stream()
                .map(StudentResponse::from)
                .toList();
    }

    /** Backs GET /api/students/{id}. Throws ResourceNotFoundException (-> 404) if the id doesn't exist. */
    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return StudentResponse.from(findStudentOrThrow(id));
    }

    /**
     * Backs POST /api/students.
     * Checks for a duplicate code up front (rather than letting the database
     * constraint fail) so the caller gets a clear 409 with a helpful message
     * instead of a generic database error. cohortId/mentorId are optional —
     * only resolved and attached if the caller actually supplied them.
     */
    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        if (studentRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("A student with code '" + request.code() + "' already exists");
        }
        Student student = new Student();
        student.setCode(request.code());
        student.setName(request.name());
        student.setEmail(request.email());
        student.setStatus(request.status() != null ? request.status() : StudentStatus.ACTIVE);
        if (request.cohortId() != null) {
            student.setCohort(findCohortOrThrow(request.cohortId()));
        }
        if (request.mentorId() != null) {
            student.setMentor(findMentorOrThrow(request.mentorId()));
        }
        return StudentResponse.from(studentRepository.save(student));
    }

    /**
     * Backs PUT /api/students/{id}. This is a PARTIAL update: each field is
     * only overwritten if the request actually supplied a non-blank/non-null
     * value for it, so callers can update just one field (e.g. only `status`)
     * without needing to resend the entire student.
     */
    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {
        Student student = findStudentOrThrow(id);
        if (request.code() != null && !request.code().isBlank()) {
            student.setCode(request.code());
        }
        if (request.name() != null && !request.name().isBlank()) {
            student.setName(request.name());
        }
        if (request.email() != null && !request.email().isBlank()) {
            student.setEmail(request.email());
        }
        if (request.status() != null) {
            student.setStatus(request.status());
        }
        if (request.cohortId() != null) {
            student.setCohort(findCohortOrThrow(request.cohortId()));
        }
        if (request.mentorId() != null) {
            student.setMentor(findMentorOrThrow(request.mentorId()));
        }
        // save() on an already-persisted entity issues an UPDATE, not an INSERT
        // (JPA/Hibernate detects this from the entity already having a non-null id).
        return StudentResponse.from(studentRepository.save(student));
    }

    /** Backs PUT /api/students/{id}/mentor/{mentorId}. Both ids must reference existing rows. */
    @Transactional
    public StudentResponse assignMentor(Long studentId, Long mentorId) {
        Student student = findStudentOrThrow(studentId);
        student.setMentor(findMentorOrThrow(mentorId));
        return StudentResponse.from(studentRepository.save(student));
    }

    /**
     * Backs PUT /api/students/{id}/cohort. Unlike assignMentor above, this
     * takes a cohort NAME, not an id, and will create the cohort if a cohort
     * with that name doesn't exist yet (find-or-create) — this keeps the
     * endpoint convenient for the common case of "put this student in cohort
     * '2026'" without requiring the caller to first look up or create a
     * cohort id separately.
     */
    @Transactional
    public StudentResponse assignCohort(Long studentId, String cohortName) {
        Student student = findStudentOrThrow(studentId);
        Cohort cohort = cohortRepository.findByName(cohortName)
                .orElseGet(() -> cohortRepository.save(newCohort(cohortName)));
        student.setCohort(cohort);
        return StudentResponse.from(studentRepository.save(student));
    }

    /** Builds a new, unsaved Cohort with just its name set — used by the find-or-create logic in assignCohort. */
    private Cohort newCohort(String name) {
        Cohort cohort = new Cohort();
        cohort.setName(name);
        return cohort;
    }

    // --- Shared lookup helpers: each throws a 404-mapped exception instead of
    // returning null, so callers never need a manual null check. ---

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));
    }

    private Mentor findMentorOrThrow(Long id) {
        return mentorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Mentor", id));
    }

    private Cohort findCohortOrThrow(Long id) {
        return cohortRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Cohort", id));
    }
}
