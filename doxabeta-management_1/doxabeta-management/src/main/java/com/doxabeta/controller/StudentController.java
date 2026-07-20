package com.doxabeta.controller;

import com.doxabeta.dto.CohortAssignRequest;
import com.doxabeta.dto.StudentCreateRequest;
import com.doxabeta.dto.StudentResponse;
import com.doxabeta.dto.StudentUpdateRequest;
import com.doxabeta.entity.StudentStatus;
import com.doxabeta.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP layer for student resources, mounted at /api/students. Controllers in
 * this app are intentionally thin — request/response mapping and validation
 * triggers only — with all real logic delegated to {@link StudentService}.
 * See SecurityConfig for which roles can call which methods here.
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * GET /api/students[?mentorId=&cohort=&status=]
     * All three query params are optional and combine as AND filters when
     * present (see StudentRepository.search). Example:
     * GET /api/students?status=ACTIVE&cohort=2026
     */
    @GetMapping
    public List<StudentResponse> list(
            @RequestParam(required = false) Long mentorId,
            @RequestParam(required = false) String cohort,
            @RequestParam(required = false) StudentStatus status
    ) {
        return studentService.search(mentorId, cohort, status);
    }

    /** GET /api/students/{id} — 404 (via ResourceNotFoundException) if the id doesn't exist. */
    @GetMapping("/{id}")
    public StudentResponse getById(@PathVariable Long id) {
        return studentService.getById(id);
    }

    /**
     * POST /api/students — creates a new student.
     * @Valid triggers Jakarta Bean Validation against StudentCreateRequest's
     * annotations before this method body runs (see that class). Returns
     * HTTP 201 Created (via @ResponseStatus) rather than the default 200.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody StudentCreateRequest request) {
        return studentService.create(request);
    }

    /** PUT /api/students/{id} — partial update; see StudentUpdateRequest/StudentService.update for the semantics. */
    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        return studentService.update(id, request);
    }

    /** PUT /api/students/{id}/mentor/{mentorId} — assigns (or reassigns) a student's mentor. No request body. */
    @PutMapping("/{id}/mentor/{mentorId}")
    public StudentResponse assignMentor(@PathVariable Long id, @PathVariable Long mentorId) {
        return studentService.assignMentor(id, mentorId);
    }

    /**
     * PUT /api/students/{id}/cohort — assigns a student to a cohort by NAME,
     * e.g. body {"cohort": "2026"}. Creates the cohort if it doesn't exist yet
     * (see StudentService.assignCohort).
     */
    @PutMapping("/{id}/cohort")
    public StudentResponse assignCohort(@PathVariable Long id, @Valid @RequestBody CohortAssignRequest request) {
        return studentService.assignCohort(id, request.cohort());
    }
}
