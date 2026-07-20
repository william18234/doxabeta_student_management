package com.doxabeta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests proving the security rules in {@link com.doxabeta.config.SecurityConfig}
 * actually behave the way they're documented to. These don't test business
 * logic (that would belong in unit tests against the service layer) — the
 * focus here is purely "does the right HTTP status come back for the right
 * role", which is exactly the kind of thing that's easy to silently break
 * while editing SecurityConfig.
 *
 * @AutoConfigureMockMvc gives us a MockMvc client that sends fake HTTP
 * requests straight into Spring's dispatcher servlet — no real network port
 * is opened, but the full filter chain (including security) still runs.
 *
 * @WithMockUser (from the spring-security-test dependency) simulates an
 * already-authenticated user with the given role for a single test method,
 * without needing to construct a real Basic-auth header.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /** No credentials at all -> Spring Security should reject with 401 Unauthorized before the controller ever runs. */
    @Test
    void listStudentsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    /** A STUDENT is allowed to GET data — confirms the "GET /api/** -> all three roles" rule. */
    @Test
    @WithMockUser(roles = "STUDENT")
    void listStudentsReturnsSeededDataForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());
    }

    /**
     * A STUDENT trying to POST a new student should be blocked with 403
     * Forbidden (not 401 — they ARE authenticated, they just lack permission).
     * Confirms the "other POST/PUT -> ADMIN/MENTOR only" rule.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void studentCannotCreateStudent() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType("application/json")
                        .content("{\"code\":\"STU999\",\"name\":\"Test User\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isForbidden());
    }

    /** An ADMIN can reach the admin-only overview endpoint. Confirms the "/api/admin/** -> ADMIN only" rule (positive case). */
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminOverviewReturnsCounts() throws Exception {
        mockMvc.perform(get("/api/admin/overview"))
                .andExpect(status().isOk());
    }
}
