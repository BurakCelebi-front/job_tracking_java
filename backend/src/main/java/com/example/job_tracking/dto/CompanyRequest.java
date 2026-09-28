package com.example.job_tracking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyRequest {
    @NotBlank(message = "Company name can not be empty!")
    private String name;
    
    private String website;
    private String location;
}
