package com.example.job_tracking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.job_tracking.dto.AuthResponse;
import com.example.job_tracking.dto.LoginRequest;
import com.example.job_tracking.dto.RegisterRequest;
import com.example.job_tracking.dto.UserResponse;
import com.example.job_tracking.entity.Application;
import com.example.job_tracking.entity.Company;
import com.example.job_tracking.entity.PasswordResetToken;
import com.example.job_tracking.entity.User;
import com.example.job_tracking.repository.ApplicationRepository;
import com.example.job_tracking.repository.CompanyRepository;
import com.example.job_tracking.repository.PasswordResetTokenRepository;
import com.example.job_tracking.repository.StatusHistoryRepository;
import com.example.job_tracking.repository.UserRepository;
import com.example.job_tracking.security.JwtService;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository,
                        CompanyRepository companyRepository,
                        ApplicationRepository applicationRepository,
                        StatusHistoryRepository statusHistoryRepository,
                        PasswordResetTokenRepository passwordResetTokenRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        AuthenticationManager authenticationManager,
                        EmailService emailService) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public AuthResponse register(RegisterRequest request){
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exist.");
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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtService.generateToken(request.getEmail());
        return new AuthResponse(token);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail());
    }

    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        List<Application> applications = applicationRepository.findByUser(user);
        for (Application application : applications) {
            statusHistoryRepository.deleteByApplication(application);
        }
        applicationRepository.deleteAll(applications);

        List<Company> companies = companyRepository.findByOwner(user);
        companyRepository.deleteAll(companies);

        userRepository.delete(user);
    }

    // Always completes normally whether or not the email is registered — the
    // controller returns the same response either way, so this endpoint
    // can't be used to probe which emails have an account.
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUser(user);
            resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken.getToken();
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link."));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired reset link.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}