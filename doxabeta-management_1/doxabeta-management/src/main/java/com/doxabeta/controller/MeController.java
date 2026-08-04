package com.doxabeta.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public Map<String, Object> me(Authentication authentication) {
        String username = authentication.getName();

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_STUDENT")
                .replace("ROLE_", "");

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("name", username);
        response.put("email", username + "@doxabeta.com");
        response.put("role", role);

        return response;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "doxabeta-management");
        response.put("status", "UP");
        return response;
    }
}
