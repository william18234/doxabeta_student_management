# Doxabeta Management System Backend

Spring Boot prototype backend for the Doxabeta Management System. It provides REST APIs for students, mentors, cohorts, daily hours, assignments, reviews, CSV seeding, Swagger documentation, and basic role-based access control.

## Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- H2 for local testing
- PostgreSQL profile for deployment
- OpenCSV for seed imports
- Springdoc OpenAPI / Swagger UI

## Open in an IDE

**IntelliJ IDEA**: `File > Open`, select this folder, and let it import as a Maven project. Once indexing finishes, run the `com.doxabeta.Application` class (green arrow next to `main`), or use the Maven tool window to run `spring-boot:run`.

**VS Code**: install the "Extension Pack for Java" and "Spring Boot Extension Pack", then open this folder — it will detect the Maven project automatically.

The first build needs an internet connection so Maven can download the project's dependencies (Spring Boot, H2, PostgreSQL driver, etc.) into your local `~/.m2` repository. After that first build, subsequent runs work offline.

## Run Locally

From the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

H2 console:

```text
http://localhost:8080/h2-console
```

Use this JDBC URL in H2:

```text
jdbc:h2:mem:doxadb
```

On Windows, you can also double-click `run-backend.bat` or run:

```powershell
.\run-backend.ps1
```

In IntelliJ IDEA, open this folder as a Maven project, wait for dependencies to load, then run `com.doxabeta.Application`.

## Demo Users

HTTP Basic auth is enabled.

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `admin123` | Admin |
| `mentor` | `mentor123` | Mentor |
| `student` | `student123` | Student |

Passwords can be overridden with `ADMIN_PASSWORD`, `MENTOR_PASSWORD`, and `STUDENT_PASSWORD`.

## How to Test

There are three ways to test this backend, from quickest to most thorough.

### 1. Automated tests

```powershell
.\mvnw.cmd test
```

This runs two test classes (`src/test/java/com/doxabeta/`):

- **`ApplicationTests`** — boots the entire application (all entities, repositories, services, controllers, security config) against an in-memory H2 database and confirms it starts without errors. This also exercises the CSV seeder, since it runs automatically on startup.
- **`StudentControllerTest`** — sends real HTTP requests through Spring's test client (`MockMvc`) and checks that the security rules actually hold: an anonymous request is rejected (401), a `STUDENT` can read data (200) but not create a student (403), and an `ADMIN` can reach the admin-only overview endpoint (200).

A green `BUILD SUCCESS` means the app wires together correctly and the access-control rules haven't regressed.

### 2. Swagger UI (interactive, no setup)

With the app running, open **http://localhost:8080/swagger-ui.html**. Every endpoint is listed with its expected request/response shape. Click **Authorize** in the top right, enter one of the demo usernames/passwords, and you can fire requests directly from the browser — useful for a quick sanity check or a live demo.

### 3. curl (scriptable, exact control)

The `-u` flag sends HTTP Basic auth credentials. Replace the host/port if you're testing a deployed instance instead of localhost.

**Read data as a student:**

```bash
curl -u student:student123 http://localhost:8080/api/students
```

**Try to create a student as a student (expect 403 Forbidden):**

```bash
curl -i -u student:student123 -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"STU999\",\"name\":\"Test User\",\"email\":\"test@example.com\"}"
```

**Create a student as a mentor or admin (expect 201 Created):**

```bash
curl -i -u mentor:mentor123 -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"STU011\",\"name\":\"Jordan Lee\",\"email\":\"jordan.lee@example.com\"}"
```

**Log hours as a student (students are allowed to write here):**

```bash
curl -i -u student:student123 -X POST http://localhost:8080/api/daily-hours \
  -H "Content-Type: application/json" \
  -d "{\"studentId\":1,\"date\":\"2026-07-13\",\"timeIn\":\"09:00\",\"timeOut\":\"16:30\",\"notes\":\"Placement work\"}"
```

**Check the seeded data counts as admin:**

```bash
curl -u admin:admin123 http://localhost:8080/api/admin/overview
```

**Re-run the seed and confirm it's idempotent** (run it twice — the second run's `studentsCreated`/`mentorsCreated`/`cohortsCreated` counts should be 0, since everything already exists):

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/admin/seed
curl -u admin:admin123 -X POST http://localhost:8080/api/admin/seed
```

**Trigger a validation error (missing required field, expect 400 with field details):**

```bash
curl -i -u admin:admin123 -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"No Code Provided\"}"
```

**Trigger a 404 (non-existent id):**

```bash
curl -i -u admin:admin123 http://localhost:8080/api/students/9999
```

If you'd rather use a GUI HTTP client (Postman, Insomnia), set the request's Authorization type to "Basic Auth" and enter one of the demo usernames/passwords — no token or header formatting needed.

### Inspecting the database directly

With the app running on the default H2 profile, open **http://localhost:8080/h2-console**, and connect using:

- JDBC URL: `jdbc:h2:mem:doxadb`
- Username: `sa`
- Password: *(leave blank)*

From there you can run plain SQL (`SELECT * FROM STUDENTS;`) to see exactly what the seeder loaded.

## PostgreSQL

Run with the `postgres` profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Optional environment variables:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/doxabeta
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
```

For GitHub, upload the full project folder or ZIP contents to a repository. The included workflow at `.github/workflows/backend-ci.yml` runs the backend tests on every push and pull request.

## Seed Data

The app seeds automatically on startup from `src/main/resources/students.csv`.

Expected columns (header row required): `studentCode, studentName, studentEmail, status, cohortName, mentorCode, mentorName, mentorEmail`. `status` must be one of `ACTIVE`, `PAUSED`, `COMPLETED`, `WITHDRAWN` (defaults to `ACTIVE` if blank or unrecognized).

Admin can also trigger seeding:

```http
POST /api/admin/seed
Authorization: Basic admin:admin123
```

Seeding is idempotent and uses student code, mentor code, and cohort name to avoid duplicate records — see "How to Test" above for a curl example that proves this.

## Main Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/students` | List students, optional `mentorId`, `cohort`, or `status` |
| `POST` | `/api/students` | Create a student |
| `PUT` | `/api/students/{id}` | Update a student |
| `PUT` | `/api/students/{id}/mentor/{mentorId}` | Assign mentor |
| `PUT` | `/api/students/{id}/cohort` | Assign cohort with body `{ "cohort": "2026" }` |
| `GET` | `/api/mentors` | List mentors |
| `POST` | `/api/mentors` | Create a mentor |
| `GET` | `/api/mentors/{id}/students` | List mentor's students |
| `GET` | `/api/cohorts` | List cohorts |
| `POST` | `/api/cohorts` | Create a cohort |
| `GET` | `/api/cohorts/{id}/students` | List cohort students |
| `POST` | `/api/daily-hours` | Log hours |
| `GET` | `/api/daily-hours?studentId=1` | List student hours (omit for all) |
| `POST` | `/api/reviews` | Create mentor review |
| `GET` | `/api/reviews?studentId=1` | List student reviews (omit for all) |
| `POST` | `/api/assignments` | Submit assignment |
| `GET` | `/api/assignments?studentId=1` | List student assignments (omit for all) |
| `PUT` | `/api/assignments/{id}/grade` | Grade assignment |
| `GET` | `/api/admin/overview` | Counts and API health |
| `GET` | `/api/admin/raw-json` | Admin JSON data view |

## Example Requests

Log hours:

```json
{
  "studentId": 1,
  "date": "2026-07-13",
  "timeIn": "09:00",
  "timeOut": "16:30",
  "notes": "Placement project work"
}
```

Create review:

```json
{
  "studentId": 1,
  "mentorId": 1,
  "reviewDate": "2026-07-13",
  "score": 4,
  "learningOutcomes": "Completed API integration practice.",
  "notes": "Good progress.",
  "nextSteps": "Prepare weekly reflection.",
  "status": "PUBLISHED"
}
```

Submit assignment:

```json
{
  "studentId": 1,
  "title": "Week 3 API Integration Exercise",
  "description": "Connect the Webflow prototype to the /api/students endpoint."
}
```

Grade assignment (`PUT /api/assignments/{id}/grade`):

```json
{
  "grade": 88,
  "feedback": "Solid implementation, tighten up error handling next time."
}
```

Create a student:

```json
{
  "code": "STU011",
  "name": "Jordan Lee",
  "email": "jordan.lee@example.com",
  "status": "ACTIVE",
  "cohortId": 1,
  "mentorId": 1
}
```

## Access Control

All `/api/**` routes require HTTP Basic auth (one of the demo users above). Roles are enforced as follows:

| Action | Allowed roles |
| --- | --- |
| `GET` any `/api/**` resource | Admin, Mentor, Student |
| `POST /api/daily-hours`, `POST /api/assignments` | Admin, Mentor, Student (students log their own hours/work) |
| Other `POST`/`PUT` under `/api/**` (students, mentors, cohorts, reviews, grading) | Admin, Mentor |
| `/api/admin/**` | Admin only |

## Error Responses

Validation errors, missing resources, and duplicate records return a consistent JSON body, e.g.:

```json
{
  "timestamp": "2026-07-16T09:12:00",
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with id: 999",
  "path": "/api/students/999"
}
```
