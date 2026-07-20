package com.doxabeta.service;

import com.doxabeta.dto.ReviewCreateRequest;
import com.doxabeta.dto.ReviewResponse;
import com.doxabeta.entity.Mentor;
import com.doxabeta.entity.Review;
import com.doxabeta.entity.ReviewStatus;
import com.doxabeta.entity.Student;
import com.doxabeta.exception.ResourceNotFoundException;
import com.doxabeta.repository.MentorRepository;
import com.doxabeta.repository.ReviewRepository;
import com.doxabeta.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for mentor reviews of students.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;

    /**
     * Backs GET /api/reviews. If studentId is provided, returns only that
     * student's reviews; otherwise returns every review (both sorted
     * most-recent-review-date-first).
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> findByStudent(Long studentId) {
        List<Review> results = studentId != null
                ? reviewRepository.findByStudentIdOrderByReviewDateDesc(studentId)
                : reviewRepository.findAllByOrderByReviewDateDesc();
        return results.stream().map(ReviewResponse::from).toList();
    }

    /**
     * Backs POST /api/reviews. Both the student and mentor referenced in the
     * request must already exist (404 if either doesn't). If the caller
     * doesn't specify a status, it defaults to DRAFT rather than requiring
     * every request to spell it out.
     */
    @Transactional
    public ReviewResponse create(ReviewCreateRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> ResourceNotFoundException.of("Student", request.studentId()));
        Mentor mentor = mentorRepository.findById(request.mentorId())
                .orElseThrow(() -> ResourceNotFoundException.of("Mentor", request.mentorId()));
        Review review = new Review();
        review.setStudent(student);
        review.setMentor(mentor);
        review.setReviewDate(request.reviewDate());
        review.setScore(request.score());
        review.setLearningOutcomes(request.learningOutcomes());
        review.setNotes(request.notes());
        review.setNextSteps(request.nextSteps());
        review.setStatus(request.status() != null ? request.status() : ReviewStatus.DRAFT);
        return ReviewResponse.from(reviewRepository.save(review));
    }
}
