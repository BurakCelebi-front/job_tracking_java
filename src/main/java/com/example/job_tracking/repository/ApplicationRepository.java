package com.example.job_tracking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.job_tracking.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long>{
    
}
