package com.example.job_tracking.controller;

import com.example.job_tracking.dto.CompanyRequest;
import com.example.job_tracking.dto.CompanyResponse;
import com.example.job_tracking.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request, Authentication authentication) {
        return ResponseEntity.ok(companyService.createCompany(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getMyCompanies(Authentication authentication) {
        return ResponseEntity.ok(companyService.getMyCompanies(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(companyService.getCompanyById(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable Long id,
                                                            @Valid @RequestBody CompanyRequest request,
                                                            Authentication authentication) {
        return ResponseEntity.ok(companyService.updateCompany(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id, Authentication authentication) {
        companyService.deleteCompany(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}