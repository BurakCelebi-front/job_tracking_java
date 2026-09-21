package com.example.job_tracking.repository;

import com.example.job_tracking.entity.Company;
import com.example.job_tracking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByOwner(User owner);
    Optional<Company> findByIdAndOwner(Long id, User owner);
}
