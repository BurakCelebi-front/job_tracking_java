package com.example.job_tracking.controller;

import com.example.job_tracking.dto.*;
import com.example.job_tracking.entity.ApplicationStatus;
import com.example.job_tracking.service.ApplicationService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;


import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@Valid @RequestBody ApplicationRequest request,
                                                                   Authentication authentication) {
        return ResponseEntity.ok(applicationService.createApplication(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getMyApplications(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(applicationService.getApplicationById(id, authentication.getName()));
    }

    @GetMapping("/search")
public ResponseEntity<Page<ApplicationResponse>> searchApplications(
        Authentication authentication,
        @RequestParam(required = false) ApplicationStatus status,
        @RequestParam(required = false) Long companyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    // Clamp paging input so a client cannot request huge pages.
    Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
    Page<ApplicationResponse> result = applicationService.searchApplications(
        authentication.getName(), status, companyId, pageable);
    return ResponseEntity.ok(result);
}
    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(@PathVariable Long id,
                                                                   @Valid @RequestBody ApplicationRequest request,
                                                                   Authentication authentication) {
        return ResponseEntity.ok(applicationService.updateApplication(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(@PathVariable Long id,
                                                              @Valid @RequestBody StatusUpdateRequest request,
                                                                Authentication authentication) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request, authentication.getName()));
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<List<StatusHistoryResponse>> getStatusHistory(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(applicationService.getStatusHistory(id, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id, Authentication authentication) {
        applicationService.deleteApplication(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}