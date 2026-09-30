package com.example.job_tracking.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_tracking.dto.AuthResponse;
import com.example.job_tracking.dto.LoginRequest;
import com.example.job_tracking.dto.RegisterRequest;
import com.example.job_tracking.dto.UserResponse;
import com.example.job_tracking.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Returns the currently authenticated user's fullName + email, read from
    // the JWT. Authentication.getName() is the email — JwtService issues the
    // token with the email as subject (see login()/register() above), so
    // whatever authenticates the request already carries it as principal.
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }
}