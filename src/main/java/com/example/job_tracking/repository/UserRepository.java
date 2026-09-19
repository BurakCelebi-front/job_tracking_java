package com.example.job_tracking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.job_tracking.entity.User;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
}
