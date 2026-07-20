package com.doxabeta.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Expected JSON body for POST /api/daily-hours, e.g.:
 * {"studentId": 1, "date": "2026-07-13", "timeIn": "09:00", "timeOut": "16:30", "notes": "..."}
 *
 * DailyHoursService additionally validates that timeOut is after timeIn — that
 * business rule can't be expressed as a simple field annotation here since it
 * involves comparing two fields, so it lives in the service layer instead.
 */
public record DailyHoursCreateRequest(
        @NotNull Long studentId,
        @NotNull LocalDate date,
        @NotNull LocalTime timeIn,
        @NotNull LocalTime timeOut,
        String notes
) {
}
