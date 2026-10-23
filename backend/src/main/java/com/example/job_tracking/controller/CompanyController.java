package com.example.job_tracking.controller;

import com.example.job_tracking.dto.CompanyRequest;
import com.example.job_tracking.dto.CompanyResponse;
import com.example.job_tracking.service.CompanyService;
import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request, Authentication authentication) {
        return ResponseEntity.ok(companyService.createCompany(request, authentication.getName()));
    }

    @GetMapping("/search")
public ResponseEntity<Page<CompanyResponse>> searchCompanies(
        Authentication authentication,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String location,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    // Clamp paging input so a client cannot request huge pages.
    Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
    Page<CompanyResponse> result = companyService.searchCompanies(
            authentication.getName(), name, location, pageable);
    return ResponseEntity.ok(result);
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