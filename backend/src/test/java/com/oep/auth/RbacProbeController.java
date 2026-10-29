package com.oep.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * TEST-ONLY controller (lives in src/test, never shipped).
 * The real instructor/admin/student endpoints don't exist yet (Sprint 1 Step 1.5+),
 * so these probes give the RBAC tests a real 200 response to assert against.
 * Role rules come from SecurityConfig, not from this class.
 */
@RestController
class RbacProbeController {

    @GetMapping("/api/v1/admin/probe")
    Map<String, String> admin() {
        return Map.of("area", "admin");
    }

    @GetMapping("/api/v1/instructor/probe")
    Map<String, String> instructor() {
        return Map.of("area", "instructor");
    }

    @GetMapping("/api/v1/student/probe")
    Map<String, String> student() {
        return Map.of("area", "student");
    }
}
