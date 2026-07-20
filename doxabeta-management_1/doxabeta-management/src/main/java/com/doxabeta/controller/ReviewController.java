package com.doxabeta.controller;

import com.doxabeta.dto.ReviewCreateRequest;
import com.doxabeta.dto.ReviewResponse;
import com.doxabeta.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP layer for mentor reviews, mounted at /api/reviews.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** GET /api/reviews[?studentId=] — omit studentId to list every review across all students. */
    @GetMapping
    public List<ReviewResponse> list(@RequestParam(required = false) Long studentId) {
        return reviewService.findByStudent(studentId);
    }

    /** POST /api/reviews — creates a new review. Restricted to ADMIN/MENTOR (see SecurityConfig). Returns HTTP 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.create(request);
    }
}
