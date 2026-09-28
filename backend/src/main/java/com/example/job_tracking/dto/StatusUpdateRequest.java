package com.example.job_tracking.dto;

import com.example.job_tracking.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequest {
    @NotNull(message = "New status is required")
    private ApplicationStatus status;

    private String note;
}
