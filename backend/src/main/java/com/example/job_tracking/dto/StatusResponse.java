package com.example.job_tracking.dto;

import com.example.job_tracking.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {
    private Long id;
    private ApplicationStatus status;
    private LocalDateTime changedAt;
    private String note;
}
