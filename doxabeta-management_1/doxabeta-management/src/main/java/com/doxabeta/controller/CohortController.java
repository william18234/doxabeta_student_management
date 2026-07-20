package com.doxabeta.controller;

import com.doxabeta.dto.CohortCreateRequest;
import com.doxabeta.dto.CohortResponse;
import com.doxabeta.dto.StudentResponse;
import com.doxabeta.service.CohortService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP layer for cohort resources, mounted at /api/cohorts.
 */
@RestController
@RequestMapping("/api/cohorts")
@RequiredArgsConstructor
public class CohortController {

    private final CohortService cohortService;

    /** GET /api/cohorts — every cohort, each with its current student count. */
    @GetMapping
    public List<CohortResponse> list() {
        return cohortService.findAll();
    }

    /** GET /api/cohorts/{id}/students — every student currently in this cohort. */
    @GetMapping("/{id}/students")
    public List<StudentResponse> getStudents(@PathVariable Long id) {
        return cohortService.getStudents(id);
    }

    /**
     * POST /api/cohorts — creates a new (empty) cohort directly. Most cohorts
     * in practice get created implicitly via PUT /api/students/{id}/cohort
     * instead; this endpoint exists for setting one up ahead of time.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CohortResponse create(@Valid @RequestBody CohortCreateRequest request) {
        return cohortService.create(request);
    }
}
