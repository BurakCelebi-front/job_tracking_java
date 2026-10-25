package com.example.job_tracking.service;

import java.util.List;

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
import com.example.job_tracking.entity.User;
import com.example.job_tracking.repository.ApplicationRepository;
import com.example.job_tracking.repository.CompanyRepository;
import com.example.job_tracking.repository.StatusHistoryRepository;
import com.example.job_tracking.repository.UserRepository;
import com.example.job_tracking.security.JwtService;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                        CompanyRepository companyRepository,
                        ApplicationRepository applicationRepository,
                        StatusHistoryRepository statusHistoryRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
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

    // Deletes the account and everything that belongs to it. No DB-level
    // cascade exists between these entities, so child rows are removed
    // ourselves, in FK-safe order: StatusHistory -> Application -> Company -> User.
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
}