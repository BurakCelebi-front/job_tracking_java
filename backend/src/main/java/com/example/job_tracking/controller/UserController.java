package com.example.job_tracking.controller;

import com.example.job_tracking.dto.UserResponse;
import com.example.job_tracking.entity.User;
import com.example.job_tracking.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController 
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserResponse response = new UserResponse(user.getId(), user.getFullName(), user.getEmail());

        return ResponseEntity.ok(response);
    }
    
}
