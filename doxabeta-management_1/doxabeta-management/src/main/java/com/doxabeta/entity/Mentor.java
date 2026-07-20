package com.doxabeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity mapped to the "mentors" table. Mentors are assigned to students
 * (see Student.mentor) and write {@link Review}s for the students they supervise.
 *
 * Like {@link Student}, this entity is converted to
 * {@link com.doxabeta.dto.MentorResponse} before being returned by the API —
 * see the class comment on Student for why.
 */
@Entity
@Table(name = "mentors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mentor {

    /** Auto-incrementing primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-assigned, unique mentor identifier (e.g. "MEN001"), used by the CSV seed and API. */
    @NotBlank
    @Column(nullable = false, unique = true)
    private String code;

    /** Full display name. */
    @NotBlank
    @Column(nullable = false)
    private String name;

    /** Contact email. Validated for format but not required to be unique. */
    @Email
    private String email;

    /**
     * The inverse side of the Student.mentor relationship — i.e. "which students
     * does this mentor have". `mappedBy = "mentor"` tells Hibernate that the
     * `mentor` column lives on the Student table, so this collection is
     * read-only from Hibernate's perspective (you can't add to this list to
     * create the association; you set Student.mentor instead).
     *
     * @JsonIgnore prevents Jackson from ever trying to serialize this collection,
     * which would trigger lazy-loading outside a transaction and also create a
     * circular reference back to Mentor via each Student. Controllers that need
     * "this mentor's students" use StudentRepository.findByMentorIdOrderById(...)
     * instead of touching this field (see MentorService.getStudents).
     */
    @OneToMany(mappedBy = "mentor", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Student> students = new ArrayList<>();
}
