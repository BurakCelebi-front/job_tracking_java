package com.example.job_tracking.repository;


import com.example.job_tracking.entity.Application;
import com.example.job_tracking.entity.User;
import com.example.job_tracking.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application>{
    List<Application> findByUser(User user);
    Optional<Application> findByIdAndUser(Long id, User user);
    List<Application> findByCompany(Company company);
}
