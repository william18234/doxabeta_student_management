package com.doxabeta.controller;

import com.doxabeta.dto.MentorCreateRequest;
import com.doxabeta.dto.MentorResponse;
import com.doxabeta.dto.StudentResponse;
import com.doxabeta.service.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP layer for mentor resources, mounted at /api/mentors.
 */
@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    /** GET /api/mentors — full mentor list, no filters. */
    @GetMapping
    public List<MentorResponse> list() {
        return mentorService.findAll();
    }

    /** GET /api/mentors/{id} — single mentor lookup; 404 if not found. */
    @GetMapping("/{id}")
    public MentorResponse getById(@PathVariable Long id) {
        return mentorService.getById(id);
    }

    /** GET /api/mentors/{id}/students — every student currently assigned to this mentor. */
    @GetMapping("/{id}/students")
    public List<StudentResponse> getStudents(@PathVariable Long id) {
        return mentorService.getStudents(id);
    }

    /** POST /api/mentors — creates a new mentor. Returns HTTP 201 Created. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MentorResponse create(@Valid @RequestBody MentorCreateRequest request) {
        return mentorService.create(request);
    }
}
