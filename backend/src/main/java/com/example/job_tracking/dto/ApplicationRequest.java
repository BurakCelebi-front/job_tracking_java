package com.example.job_tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
public class ApplicationRequest {
    @NotNull(message = "Company ID required")
    private Long companyId;

    @NotBlank(message = "Position name can not be empty")
    private String positionTitle;

    private LocalDate appliedDate;
    private String notes;
}
