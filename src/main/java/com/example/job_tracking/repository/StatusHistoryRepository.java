package com.example.job_tracking.repository;

import com.example.job_tracking.entity.Application;
import com.example.job_tracking.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByApplicationOrderByChangedAtAsc(Application application);
    void deleteByApplication(Application application);
}
