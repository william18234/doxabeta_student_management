package com.doxabeta.repository;

import com.doxabeta.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data-access layer for {@link Review}.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** All reviews for one student, most recent review date first. Used by GET /api/reviews?studentId=... */
    List<Review> findByStudentIdOrderByReviewDateDesc(Long studentId);

    /** All reviews across every student, most recent first. Used by GET /api/reviews with no studentId. */
    List<Review> findAllByOrderByReviewDateDesc();
}
