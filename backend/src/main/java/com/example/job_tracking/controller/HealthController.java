package com.example.job_tracking.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_tracking.repository.UserRepository;

/**
 * Public health check that also touches the database.
 * Pinged on a schedule (see .github/workflows/keep-alive.yml) so the free Render instance
 * and the free Supabase project don't get paused for inactivity.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final UserRepository userRepository;

    public HealthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public Map<String, String> health() {
        userRepository.count();
        return Map.of("status", "ok");
    }
}
