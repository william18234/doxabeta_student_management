package com.doxabeta.controller;

import com.doxabeta.dto.AdminOverviewResponse;
import com.doxabeta.dto.AdminRawJsonResponse;
import com.doxabeta.dto.SeedResult;
import com.doxabeta.service.AdminService;
import com.doxabeta.service.SeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP layer for admin-only endpoints, mounted at /api/admin. Every route
 * here requires the ADMIN role (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SeedService seedService;

    /** GET /api/admin/overview — record counts per table plus a status flag, for a dashboard/health check. */
    @GetMapping("/overview")
    public AdminOverviewResponse overview() {
        return adminService.overview();
    }

    /** GET /api/admin/raw-json — dumps every record in the system as one JSON payload. Debugging/demo use only. */
    @GetMapping("/raw-json")
    public AdminRawJsonResponse rawJson() {
        return adminService.rawJson();
    }

    /**
     * POST /api/admin/seed — manually (re-)runs the CSV seed described in
     * {@link SeedService}, on top of the automatic seed that already happens
     * at application startup. Safe to call repeatedly (see SeedService's
     * idempotency notes). `throws Exception` is allowed to propagate to
     * GlobalExceptionHandler's generic handler, which turns any failure into
     * a clean 500 JSON response instead of a raw stack trace.
     */
    @PostMapping("/seed")
    public SeedResult seed() throws Exception {
        return seedService.seedFromClasspath();
    }
}
