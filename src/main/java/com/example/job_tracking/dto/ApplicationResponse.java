package com.example.job_tracking.dto;

import com.example.job_tracking.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private String postionName;
    private ApplicationStatus status;
    private LocalDate appliedDate;
    private String notes;

}
