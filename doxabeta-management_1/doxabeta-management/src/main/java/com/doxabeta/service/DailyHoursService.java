package com.doxabeta.service;

import com.doxabeta.dto.DailyHoursCreateRequest;
import com.doxabeta.dto.DailyHoursResponse;
import com.doxabeta.entity.DailyHours;
import com.doxabeta.entity.Student;
import com.doxabeta.exception.ResourceNotFoundException;
import com.doxabeta.repository.DailyHoursRepository;
import com.doxabeta.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for daily hours log entries.
 */
@Service
@RequiredArgsConstructor
public class DailyHoursService {

    private final DailyHoursRepository dailyHoursRepository;
    private final StudentRepository studentRepository;

    /**
     * Backs GET /api/daily-hours. If studentId is provided, returns only that
     * student's entries; otherwise returns every entry across all students
     * (both sorted most-recent-first).
     */
    @Transactional(readOnly = true)
    public List<DailyHoursResponse> findByStudent(Long studentId) {
        List<DailyHours> results = studentId != null
                ? dailyHoursRepository.findByStudentIdOrderByDateDesc(studentId)
                : dailyHoursRepository.findAllByOrderByDateDesc();
        return results.stream().map(DailyHoursResponse::from).toList();
    }

    /**
     * Backs POST /api/daily-hours.
     * Two checks happen before saving: the referenced student must exist
     * (404 if not), and timeOut must be after timeIn (400 if not — this is a
     * cross-field business rule that can't be expressed as a simple
     * @NotNull/@Min-style annotation on the request DTO, so it lives here).
     */
    @Transactional
    public DailyHoursResponse create(DailyHoursCreateRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> ResourceNotFoundException.of("Student", request.studentId()));
        if (!request.timeOut().isAfter(request.timeIn())) {
            throw new IllegalArgumentException("timeOut must be after timeIn");
        }
        DailyHours entry = new DailyHours();
        entry.setStudent(student);
        entry.setDate(request.date());
        entry.setTimeIn(request.timeIn());
        entry.setTimeOut(request.timeOut());
        entry.setNotes(request.notes());
        return DailyHoursResponse.from(dailyHoursRepository.save(entry));
    }
}
