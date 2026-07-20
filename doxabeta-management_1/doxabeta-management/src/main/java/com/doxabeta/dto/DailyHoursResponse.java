package com.doxabeta.dto;

import com.doxabeta.entity.DailyHours;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * What a DailyHours log entry looks like over the API.
 *
 * hoursWorked is NOT stored in the database — it's derived from timeIn/timeOut
 * every time this DTO is built, so it's always consistent with the raw times
 * and there's no risk of a stale/incorrect stored value.
 */
public record DailyHoursResponse(
        Long id,
        Long studentId,
        String studentName,
        LocalDate date,
        LocalTime timeIn,
        LocalTime timeOut,
        double hoursWorked,
        String notes
) {
    /** Converts a JPA entity into this response shape, computing hoursWorked along the way. */
    public static DailyHoursResponse from(DailyHours d) {
        double hours = 0.0;
        if (d.getTimeIn() != null && d.getTimeOut() != null) {
            // Duration.between gives whole minutes; dividing by 60.0 converts to
            // fractional hours (e.g. 90 minutes -> 1.5 hours).
            hours = Duration.between(d.getTimeIn(), d.getTimeOut()).toMinutes() / 60.0;
        }
        return new DailyHoursResponse(
                d.getId(),
                d.getStudent() != null ? d.getStudent().getId() : null,
                d.getStudent() != null ? d.getStudent().getName() : null,
                d.getDate(),
                d.getTimeIn(),
                d.getTimeOut(),
                // Round to 2 decimal places (e.g. 7.3333... -> 7.33) for a clean display value.
                Math.round(hours * 100.0) / 100.0,
                d.getNotes()
        );
    }
}
