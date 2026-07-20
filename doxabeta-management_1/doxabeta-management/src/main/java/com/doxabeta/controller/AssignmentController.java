package com.doxabeta.controller;

import com.doxabeta.dto.AssignmentCreateRequest;
import com.doxabeta.dto.AssignmentGradeRequest;
import com.doxabeta.dto.AssignmentResponse;
import com.doxabeta.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP layer for assignment submissions and grading, mounted at /api/assignments.
 * Note in SecurityConfig: POST (submission) is allowed for STUDENT too, but
 * grading (PUT .../grade) is restricted to ADMIN/MENTOR.
 */
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    /** GET /api/assignments[?studentId=] — omit studentId to list every submission across all students. */
    @GetMapping
    public List<AssignmentResponse> list(@RequestParam(required = false) Long studentId) {
        return assignmentService.findByStudent(studentId);
    }

    /** POST /api/assignments — submits a new piece of work for a student. Returns HTTP 201 Created. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse create(@Valid @RequestBody AssignmentCreateRequest request) {
        return assignmentService.create(request);
    }

    /** PUT /api/assignments/{id}/grade — records a grade + feedback and marks the assignment GRADED. */
    @PutMapping("/{id}/grade")
    public AssignmentResponse grade(@PathVariable Long id, @Valid @RequestBody AssignmentGradeRequest request) {
        return assignmentService.grade(id, request);
    }
}
