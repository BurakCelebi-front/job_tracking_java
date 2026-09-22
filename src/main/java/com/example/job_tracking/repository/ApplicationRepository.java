package com.example.job_tracking.repository;


import com.example.job_tracking.entity.Application;
import com.example.job_tracking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long>{
    List<Application> findByUser(User user);
    Optional<Application> findByIdAndUser(Long id, User user);
}
