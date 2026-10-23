package com.example.job_tracking.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.job_tracking.dto.AuthResponse;
import com.example.job_tracking.dto.LoginRequest;
import com.example.job_tracking.dto.RegisterRequest;
import com.example.job_tracking.dto.UserResponse;
import com.example.job_tracking.entity.User;
import com.example.job_tracking.exception.ConflictException;
import com.example.job_tracking.exception.ResourceNotFoundException;
import com.example.job_tracking.exception.TooManyRequestsException;
import com.example.job_tracking.repository.UserRepository;
import com.example.job_tracking.security.JwtService;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Brute-force protection: after MAX_FAILED_LOGINS wrong passwords for the same email
    // within LOCKOUT_WINDOW, further attempts are rejected with 429 until the window passes.
    // Kept in memory, which is fine for a single instance; a multi-instance deployment
    // would move this to a shared store (e.g. Redis).
    private static final int MAX_FAILED_LOGINS = 5;
    private static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(15);
    private final Map<String, Deque<Instant>> failedLogins = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request){
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("An account with this email already exists.");
        }

        User user = new User();
        user.setFullName(request.getFullname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        String key = request.getEmail().trim().toLowerCase(Locale.ROOT);

        // Only emails with recent failures have an entry; expired ones are dropped here,
        // so the map can't grow from successful logins.
        Deque<Instant> failures = failedLogins.get(key);
        if (failures != null) {
            Instant cutoff = Instant.now().minus(LOCKOUT_WINDOW);
            Instant oldest;
            while ((oldest = failures.peekFirst()) != null && oldest.isBefore(cutoff)) {
                failures.pollFirst();
            }
            if (failures.isEmpty()) {
                failedLogins.remove(key, failures);
            } else if (failures.size() >= MAX_FAILED_LOGINS) {
                throw new TooManyRequestsException("Too many failed login attempts. Please try again in 15 minutes.");
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            failedLogins.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>()).addLast(Instant.now());
            throw e;
        }
        failedLogins.remove(key);

        String token = jwtService.generateToken(request.getEmail());
        return new AuthResponse(token);
    }

    // Uses your existing UserResponse DTO (id, fullname, email — note the
    // lowercase "fullname" there, matching its field name via @AllArgsConstructor).
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail());
    }
}