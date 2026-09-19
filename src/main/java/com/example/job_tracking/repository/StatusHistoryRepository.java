package com.example.job_tracking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.job_tracking.entity.StatusHistory;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

}
