package com.example.job_tracking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.job_tracking.entity.Company;


public interface CompanyRepository extends JpaRepository<Company, Long> {
    
}
