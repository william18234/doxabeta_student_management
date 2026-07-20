package com.doxabeta.controller;

import com.doxabeta.dto.DailyHoursCreateRequest;
import com.doxabeta.dto.DailyHoursResponse;
import com.doxabeta.service.DailyHoursService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP layer for daily-hours log entries, mounted at /api/daily-hours.
 * Note in SecurityConfig: POST here is allowed for STUDENT too (not just
 * ADMIN/MENTOR), since students log their own hours.
 */
@RestController
@RequestMapping("/api/daily-hours")
@RequiredArgsConstructor
public class DailyHoursController {

    private final DailyHoursService dailyHoursService;

    /** GET /api/daily-hours[?studentId=] — omit studentId to list every entry across all students. */
    @GetMapping
    public List<DailyHoursResponse> list(@RequestParam(required = false) Long studentId) {
        return dailyHoursService.findByStudent(studentId);
    }

    /** POST /api/daily-hours — logs a new day's hours for a student. Returns HTTP 201 Created. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DailyHoursResponse create(@Valid @RequestBody DailyHoursCreateRequest request) {
        return dailyHoursService.create(request);
    }
}
