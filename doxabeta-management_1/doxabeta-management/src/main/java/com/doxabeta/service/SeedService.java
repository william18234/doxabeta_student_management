package com.doxabeta.service;

import com.doxabeta.dto.SeedResult;
import com.doxabeta.entity.Cohort;
import com.doxabeta.entity.Mentor;
import com.doxabeta.entity.Student;
import com.doxabeta.entity.StudentStatus;
import com.doxabeta.repository.CohortRepository;
import com.doxabeta.repository.MentorRepository;
import com.doxabeta.repository.StudentRepository;
import com.opencsv.CSVReaderHeaderAware;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * Seeds the database from src/main/resources/students.csv.
 *
 * Runs automatically once on every application startup (see the
 * CommandLineRunner implementation below), and can also be triggered manually
 * at any time via POST /api/admin/seed (see AdminController.seed), which calls
 * seedFromClasspath() directly.
 *
 * IDEMPOTENT BY DESIGN: every row is matched against existing data by a
 * natural key — student code, mentor code, and cohort name — rather than by
 * database id. That means running the seed twice with the same CSV file
 * never creates duplicate rows; existing records are just updated in place
 * (see the mentorsUpdated/studentsUpdated counters in {@link SeedResult}).
 * This makes it safe to re-seed after editing students.csv, or to call
 * POST /api/admin/seed repeatedly without side effects.
 *
 * Expected CSV columns (header row required): cohortName, mentorCode,
 * mentorName, mentorEmail, studentCode, studentName, studentEmail, status.
 * See src/main/resources/students.csv for a working example.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeedService implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final CohortRepository cohortRepository;
    private final ResourceLoader resourceLoader;

    /**
     * Spring Boot automatically calls run(...) on every bean implementing
     * CommandLineRunner, once, right after the application context has fully
     * started up. This is what makes the database "self-seeding" — no manual
     * step is required after a fresh `mvnw spring-boot:run`.
     */
    @Override
    public void run(String... args) throws Exception {
        SeedResult result = seedFromClasspath();
        log.info("Startup seed complete: {} rows processed, {} cohorts created, {} mentors created/{} updated, {} students created/{} updated",
                result.rowsProcessed(), result.cohortsCreated(), result.mentorsCreated(), result.mentorsUpdated(),
                result.studentsCreated(), result.studentsUpdated());
    }

    /**
     * Reads students.csv from the classpath (i.e. src/main/resources/students.csv
     * once built) and upserts cohorts, mentors, and students from it row by row.
     * The whole operation runs inside a single database transaction (@Transactional):
     * if anything fails partway through, everything rolls back rather than
     * leaving the database half-seeded.
     *
     * @return a summary of how many rows were read and how many records of
     *         each type were newly created vs. already existed and were updated
     * @throws Exception if the CSV file can't be read or is malformed
     *         (IOException / CsvValidationException from OpenCSV) — this is
     *         allowed to propagate up to GlobalExceptionHandler's generic
     *         handler when triggered via POST /api/admin/seed, and would fail
     *         application startup if it happened during run(...) above.
     */
    @Transactional
    public SeedResult seedFromClasspath() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:students.csv");
        int rows = 0;
        int cohortsCreated = 0;
        int mentorsCreated = 0;
        int mentorsUpdated = 0;
        int studentsCreated = 0;
        int studentsUpdated = 0;

        // CSVReaderHeaderAware reads the first CSV row as column headers and
        // returns each subsequent row as a Map<columnName, cellValue> — so the
        // column order in the CSV file doesn't matter, only the header names do.
        // The try-with-resources block ensures both the file reader and CSV
        // reader are closed automatically, even if an exception is thrown.
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(reader)) {

            Map<String, String> row;
            while ((row = csvReader.readMap()) != null) {
                rows++;

                // trimToNull treats blank cells as "not provided" rather than
                // as empty strings, so e.g. a row with no cohortName simply
                // leaves that student's cohort unset instead of creating a
                // cohort literally named "".
                String cohortName = trimToNull(row.get("cohortName"));
                String mentorCode = trimToNull(row.get("mentorCode"));
                String mentorName = trimToNull(row.get("mentorName"));
                String mentorEmail = trimToNull(row.get("mentorEmail"));
                String studentCode = trimToNull(row.get("studentCode"));
                String studentName = trimToNull(row.get("studentName"));
                String studentEmail = trimToNull(row.get("studentEmail"));
                String statusRaw = trimToNull(row.get("status"));

                // --- Step 1: find-or-create the cohort for this row (by name) ---
                Cohort cohort = null;
                if (cohortName != null) {
                    Optional<Cohort> existingCohort = cohortRepository.findByName(cohortName);
                    if (existingCohort.isPresent()) {
                        cohort = existingCohort.get();
                    } else {
                        Cohort newCohort = new Cohort();
                        newCohort.setName(cohortName);
                        cohort = cohortRepository.save(newCohort);
                        cohortsCreated++;
                    }
                }

                // --- Step 2: find-or-update-or-create the mentor for this row (by code) ---
                // Unlike cohorts, an existing mentor's name/email ARE refreshed
                // from the CSV each time, so editing a mentor's details in the
                // spreadsheet and re-seeding will propagate the change.
                Mentor mentor = null;
                if (mentorCode != null) {
                    Optional<Mentor> existingMentor = mentorRepository.findByCode(mentorCode);
                    if (existingMentor.isPresent()) {
                        mentor = existingMentor.get();
                        mentor.setName(mentorName);
                        mentor.setEmail(mentorEmail);
                        mentorsUpdated++;
                    } else {
                        mentor = new Mentor();
                        mentor.setCode(mentorCode);
                        mentor.setName(mentorName);
                        mentor.setEmail(mentorEmail);
                        mentorsCreated++;
                    }
                    mentor = mentorRepository.save(mentor);
                }

                // --- Step 3: find-or-update-or-create the student for this row (by code) ---
                // and attach the cohort/mentor resolved above.
                if (studentCode != null) {
                    Optional<Student> existingStudent = studentRepository.findByCode(studentCode);
                    Student student = existingStudent.orElseGet(Student::new);
                    boolean isNew = existingStudent.isEmpty();
                    student.setCode(studentCode);
                    student.setName(studentName);
                    student.setEmail(studentEmail);
                    student.setStatus(parseStatus(statusRaw));
                    student.setCohort(cohort);
                    student.setMentor(mentor);
                    studentRepository.save(student);
                    if (isNew) {
                        studentsCreated++;
                    } else {
                        studentsUpdated++;
                    }
                }
            }
        }

        return new SeedResult(rows, cohortsCreated, mentorsCreated, mentorsUpdated, studentsCreated, studentsUpdated);
    }

    /**
     * Converts the CSV "status" cell to a {@link StudentStatus}. Falls back to
     * ACTIVE for a blank cell or a value that doesn't match any enum constant
     * (e.g. a typo in the spreadsheet) — seeding intentionally never fails
     * just because of an unrecognized status value.
     */
    private StudentStatus parseStatus(String raw) {
        if (raw == null) {
            return StudentStatus.ACTIVE;
        }
        try {
            return StudentStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return StudentStatus.ACTIVE;
        }
    }

    /** Trims whitespace and converts an empty/blank string to null, so blank CSV cells read as "not provided". */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
