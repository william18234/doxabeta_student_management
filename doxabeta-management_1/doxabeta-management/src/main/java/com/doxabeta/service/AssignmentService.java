package com.doxabeta.service;

import com.doxabeta.dto.AssignmentCreateRequest;
import com.doxabeta.dto.AssignmentGradeRequest;
import com.doxabeta.dto.AssignmentResponse;
import com.doxabeta.entity.Assignment;
import com.doxabeta.entity.AssignmentStatus;
import com.doxabeta.entity.Student;
import com.doxabeta.exception.ResourceNotFoundException;
import com.doxabeta.repository.AssignmentRepository;
import com.doxabeta.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic for student assignment submissions and grading.
 */
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;

    /**
     * Backs GET /api/assignments. If studentId is provided, returns only that
     * student's submissions; otherwise returns every submission (both sorted
     * most-recently-submitted-first).
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> findByStudent(Long studentId) {
        List<Assignment> results = studentId != null
                ? assignmentRepository.findByStudentIdOrderBySubmittedAtDesc(studentId)
                : assignmentRepository.findAllByOrderBySubmittedAtDesc();
        return results.stream().map(AssignmentResponse::from).toList();
    }

    /**
     * Backs POST /api/assignments — a student (or a mentor/admin on their
     * behalf) submitting a new piece of work. Always starts life as
     * SUBMITTED with no grade; grading happens separately via grade(...) below.
     */
    @Transactional
    public AssignmentResponse create(AssignmentCreateRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> ResourceNotFoundException.of("Student", request.studentId()));
        Assignment assignment = new Assignment();
        assignment.setStudent(student);
        assignment.setTitle(request.title());
        assignment.setDescription(request.description());
        assignment.setSubmittedAt(LocalDateTime.now());
        assignment.setStatus(AssignmentStatus.SUBMITTED);
        return AssignmentResponse.from(assignmentRepository.save(assignment));
    }

    /**
     * Backs PUT /api/assignments/{id}/grade. Sets the grade and feedback and
     * flips the status to GRADED. There's no "un-grade" operation — grading
     * is treated as a one-way transition for this prototype.
     */
    @Transactional
    public AssignmentResponse grade(Long id, AssignmentGradeRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Assignment", id));
        assignment.setGrade(request.grade());
        assignment.setFeedback(request.feedback());
        assignment.setStatus(AssignmentStatus.GRADED);
        return AssignmentResponse.from(assignmentRepository.save(assignment));
    }
}
