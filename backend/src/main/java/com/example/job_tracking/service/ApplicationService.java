package com.example.job_tracking.service;

import com.example.job_tracking.dto.*;
import com.example.job_tracking.entity.*;
import com.example.job_tracking.exception.ResourceNotFoundException;
import com.example.job_tracking.repository.ApplicationRepository;
import com.example.job_tracking.repository.CompanyRepository;
import com.example.job_tracking.repository.StatusHistoryRepository;
import com.example.job_tracking.repository.UserRepository;
import com.example.job_tracking.specification.ApplicationSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                               CompanyRepository companyRepository,
                               UserRepository userRepository,
                               StatusHistoryRepository statusHistoryRepository) {
        this.applicationRepository = applicationRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ApplicationResponse toResponse(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getCompany().getId(),
                app.getCompany().getName(),
                app.getPositionTitle(),
                app.getStatus(),
                app.getAppliedDate(),
                app.getNotes()
        );
    }

    public ApplicationResponse createApplication(ApplicationRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Company company = companyRepository.findByIdAndOwner(request.getCompanyId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Application application = new Application();
        application.setUser(user);
        application.setCompany(company);
        application.setPositionTitle(request.getPositionTitle());
        application.setAppliedDate(request.getAppliedDate() != null ? request.getAppliedDate() : LocalDate.now());
        application.setNotes(request.getNotes());
        application.setStatus(ApplicationStatus.APPLIED);

        Application saved = applicationRepository.save(application);

        StatusHistory history = new StatusHistory();
        history.setApplication(saved);
        history.setStatus(ApplicationStatus.APPLIED);
        history.setChangedAt(LocalDateTime.now());
        history.setNote("Başvuru oluşturuldu");
        statusHistoryRepository.save(history);

        return toResponse(saved);
    }

    public List<ApplicationResponse> getMyApplications(String userEmail) {
        User user = getUserByEmail(userEmail);
        return applicationRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<ApplicationResponse> searchApplications(String userEmail, ApplicationStatus status,  Long companyId, Pageable pageable) {
        User user = getUserByEmail(userEmail);

        Specification<Application> spec = Specification
            .where(ApplicationSpecification.hasUser(user))
            .and(ApplicationSpecification.hasStatus(status))
            .and(ApplicationSpecification.hasCompanyId(companyId));

            Page<Application> applications = applicationRepository.findAll(spec, pageable);

            return applications.map(app -> new ApplicationResponse(
                app.getId(),
                app.getCompany().getId(),
                app.getCompany().getName(),
                app.getPositionTitle(),
                app.getStatus(),
                app.getAppliedDate(),
                app.getNotes()
            ));
    }

    public ApplicationResponse getApplicationById(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Application application = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        return toResponse(application);
    }

    public ApplicationResponse updateApplication(Long id, ApplicationRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Application application = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        Company company = companyRepository.findByIdAndOwner(request.getCompanyId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        application.setCompany(company);
        application.setPositionTitle(request.getPositionTitle());
        application.setAppliedDate(request.getAppliedDate());
        application.setNotes(request.getNotes());

        return toResponse(applicationRepository.save(application));
    }

    public ApplicationResponse updateStatus(Long id, StatusUpdateRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Application application = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        application.setStatus(request.getStatus());
        Application saved = applicationRepository.save(application);

        StatusHistory history = new StatusHistory();
        history.setApplication(saved);
        history.setStatus(request.getStatus());
        history.setChangedAt(LocalDateTime.now());
        history.setNote(request.getNote());
        statusHistoryRepository.save(history);

        return toResponse(saved);
    }

    public List<StatusHistoryResponse> getStatusHistory(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Application application = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        return statusHistoryRepository.findByApplicationOrderByChangedAtAsc(application)
                .stream()
                .map(h -> new StatusHistoryResponse(h.getId(), h.getStatus(), h.getChangedAt(), h.getNote()))
                .toList();
    }

    @Transactional
    public void deleteApplication(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Application application = applicationRepository.findByIdAndUser(id, user)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

    statusHistoryRepository.deleteByApplication(application);
    applicationRepository.delete(application);
}
}