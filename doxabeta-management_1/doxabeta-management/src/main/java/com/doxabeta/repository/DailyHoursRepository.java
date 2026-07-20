package com.doxabeta.repository;

import com.doxabeta.entity.DailyHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data-access layer for {@link DailyHours} log entries.
 */
public interface DailyHoursRepository extends JpaRepository<DailyHours, Long> {

    /** All log entries for one student, most recent date first. Used by GET /api/daily-hours?studentId=... */
    List<DailyHours> findByStudentIdOrderByDateDesc(Long studentId);

    /**
     * All log entries across every student, most recent date first.
     * "findAllByOrderBy..." (with no filter conditions between "findAllBy" and
     * "OrderBy") is Spring Data's supported syntax for "give me everything, just
     * sorted" — used when GET /api/daily-hours is called without a studentId.
     */
    List<DailyHours> findAllByOrderByDateDesc();
}
