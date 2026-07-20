package com.doxabeta.service;

import com.doxabeta.dto.MentorCreateRequest;
import com.doxabeta.dto.MentorResponse;
import com.doxabeta.dto.StudentResponse;
import com.doxabeta.entity.Mentor;
import com.doxabeta.exception.DuplicateResourceException;
import com.doxabeta.exception.ResourceNotFoundException;
import com.doxabeta.repository.MentorRepository;
import com.doxabeta.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for mentors: listing, lookup, creation, and fetching a
 * mentor's assigned students.
 */
@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorRepository mentorRepository;
    private final StudentRepository studentRepository;

    /** Backs GET /api/mentors. */
    @Transactional(readOnly = true)
    public List<MentorResponse> findAll() {
        return mentorRepository.findAll().stream().map(MentorResponse::from).toList();
    }

    /** Backs GET /api/mentors/{id}. */
    @Transactional(readOnly = true)
    public MentorResponse getById(Long id) {
        return MentorResponse.from(findMentorOrThrow(id));
    }

    /**
     * Backs GET /api/mentors/{id}/students.
     * findMentorOrThrow(mentorId) is called first purely to validate the
     * mentor exists (so a bad id returns a clear 404) — its return value is
     * discarded, since the actual student list comes from a dedicated
     * repository query rather than Mentor.students (see the comment on that
     * field in the Mentor entity for why it's avoided).
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudents(Long mentorId) {
        findMentorOrThrow(mentorId);
        return studentRepository.findByMentorIdOrderById(mentorId).stream()
                .map(StudentResponse::from)
                .toList();
    }

    /** Backs POST /api/mentors. Rejects a duplicate code with a 409 before hitting the database. */
    @Transactional
    public MentorResponse create(MentorCreateRequest request) {
        if (mentorRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("A mentor with code '" + request.code() + "' already exists");
        }
        Mentor mentor = new Mentor();
        mentor.setCode(request.code());
        mentor.setName(request.name());
        mentor.setEmail(request.email());
        return MentorResponse.from(mentorRepository.save(mentor));
    }

    private Mentor findMentorOrThrow(Long id) {
        return mentorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Mentor", id));
    }
}
