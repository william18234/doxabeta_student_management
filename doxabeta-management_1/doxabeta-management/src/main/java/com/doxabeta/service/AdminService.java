package com.doxabeta.service;

import com.doxabeta.dto.AdminOverviewResponse;
import com.doxabeta.dto.AdminRawJsonResponse;
import com.doxabeta.dto.CohortResponse;
import com.doxabeta.dto.DailyHoursResponse;
import com.doxabeta.dto.MentorResponse;
import com.doxabeta.dto.ReviewResponse;
import com.doxabeta.dto.StudentResponse;
import com.doxabeta.dto.AssignmentResponse;
import com.doxabeta.repository.AssignmentRepository;
import com.doxabeta.repository.CohortRepository;
import com.doxabeta.repository.DailyHoursRepository;
import com.doxabeta.repository.MentorRepository;
import com.doxabeta.repository.ReviewRepository;
import com.doxabeta.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only reporting/debugging endpoints for administrators. Nothing here
 * mutates data — see {@link SeedService} for the seed-triggering logic used
 * by POST /api/admin/seed.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final CohortRepository cohortRepository;
    private final DailyHoursRepository dailyHoursRepository;
    private final ReviewRepository reviewRepository;
    private final AssignmentRepository assignmentRepository;

    /**
     * Backs GET /api/admin/overview. count() issues a lightweight
     * "SELECT COUNT(*)" per table rather than loading every row, so this stays
     * cheap even as the data grows.
     */
    @Transactional(readOnly = true)
    public AdminOverviewResponse overview() {
        return new AdminOverviewResponse(
                "UP",
                studentRepository.count(),
                mentorRepository.count(),
                cohortRepository.count(),
                dailyHoursRepository.count(),
                reviewRepository.count(),
                assignmentRepository.count()
        );
    }

    /**
     * Backs GET /api/admin/raw-json. Loads every row from every table and
     * converts each to its response DTO — see AdminRawJsonResponse's class
     * comment for why this is a debugging tool, not something to build a
     * production feature on top of.
     */
    @Transactional(readOnly = true)
    public AdminRawJsonResponse rawJson() {
        return new AdminRawJsonResponse(
                studentRepository.findAll().stream().map(StudentResponse::from).toList(),
                mentorRepository.findAll().stream().map(MentorResponse::from).toList(),
                cohortRepository.findAll().stream()
                        .map(c -> CohortResponse.from(c, studentRepository.findByCohortIdOrderById(c.getId()).size()))
                        .toList(),
                dailyHoursRepository.findAllByOrderByDateDesc().stream().map(DailyHoursResponse::from).toList(),
                reviewRepository.findAllByOrderByReviewDateDesc().stream().map(ReviewResponse::from).toList(),
                assignmentRepository.findAllByOrderBySubmittedAtDesc().stream().map(AssignmentResponse::from).toList()
        );
    }
}
