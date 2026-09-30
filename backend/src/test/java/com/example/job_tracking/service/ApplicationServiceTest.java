package com.example.job_tracking.service;

/*
 * Varsayımlar (kaynağı görmediğim yerler — kendi projene göre uyarlaman gerekebilir):
 * - ApplicationRequest / StatusUpdateRequest: no-arg constructor + setter'lar (Lombok @Data tarzı)
 * - User: setId, setEmail | Company: setId, setName | StatusHistory: setId, setStatus, setChangedAt, setNote
 * - ApplicationStatus enum'unda APPLIED dışında en az bir değer daha var (aşağıda INTERVIEW kullandım — adını değiştir)
 * - ApplicationResponse ctor sırası: (id, companyId, companyName, positionTitle, status, appliedDate, notes)
 * - StatusHistoryResponse ctor sırası: (id, status, changedAt, note)
 */

import com.example.job_tracking.dto.ApplicationRequest;
import com.example.job_tracking.dto.ApplicationResponse;
import com.example.job_tracking.dto.StatusHistoryResponse;
import com.example.job_tracking.dto.StatusUpdateRequest;
import com.example.job_tracking.entity.*;
import com.example.job_tracking.repository.ApplicationRepository;
import com.example.job_tracking.repository.CompanyRepository;
import com.example.job_tracking.repository.StatusHistoryRepository;
import com.example.job_tracking.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private UserRepository userRepository;
    @Mock private StatusHistoryRepository statusHistoryRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private static final String EMAIL = "daria@example.com";

    private User user;
    private Company company;
    private Application application;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);

        company = new Company();
        company.setId(10L);
        company.setName("Acme Corp");

        application = new Application();
        application.setId(100L);
        application.setUser(user);
        application.setCompany(company);
        application.setPositionTitle("Backend Developer");
        application.setAppliedDate(LocalDate.of(2026, 1, 10));
        application.setNotes("Referral");
        application.setStatus(ApplicationStatus.APPLIED);
    }

    // ---------- createApplication ----------

    @Test
    void createApplication_success_savesApplicationAndStatusHistory() {
        ApplicationRequest request = new ApplicationRequest();
        request.setCompanyId(10L);
        request.setPositionTitle("Backend Developer");
        request.setAppliedDate(LocalDate.of(2026, 1, 10));
        request.setNotes("Referral");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.of(company));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        ApplicationResponse response = applicationService.createApplication(request, EMAIL);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCompanyId()).isEqualTo(10L);
        assertThat(response.getCompanyName()).isEqualTo("Acme Corp");
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPLIED);

        ArgumentCaptor<StatusHistory> historyCaptor = ArgumentCaptor.forClass(StatusHistory.class);
        verify(statusHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(historyCaptor.getValue().getNote()).isEqualTo("Başvuru oluşturuldu");
    }

    @Test
    void createApplication_appliedDateNull_defaultsToToday() {
        ApplicationRequest request = new ApplicationRequest();
        request.setCompanyId(10L);
        request.setPositionTitle("Backend Developer");
        request.setAppliedDate(null);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.of(company));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        applicationService.createApplication(request, EMAIL);

        ArgumentCaptor<Application> appCaptor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(appCaptor.capture());
        assertThat(appCaptor.getValue().getAppliedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createApplication_userNotFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.createApplication(new ApplicationRequest(), EMAIL));

        verifyNoInteractions(applicationRepository, companyRepository, statusHistoryRepository);
    }

    @Test
    void createApplication_companyNotFound_throws() {
        ApplicationRequest request = new ApplicationRequest();
        request.setCompanyId(99L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdAndOwner(99L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.createApplication(request, EMAIL));

        verifyNoInteractions(applicationRepository, statusHistoryRepository);
    }

    // ---------- getMyApplications ----------

    @Test
    void getMyApplications_returnsMappedList() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByUser(user)).thenReturn(List.of(application));

        List<ApplicationResponse> result = applicationService.getMyApplications(EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    @Test
    void getMyApplications_userNotFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> applicationService.getMyApplications(EMAIL));
    }

    // ---------- searchApplications ----------

    @Test
    @SuppressWarnings("unchecked")
    void searchApplications_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Application> page = new PageImpl<>(List.of(application), pageable, 1);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ApplicationResponse> result = applicationService.searchApplications(
                EMAIL, ApplicationStatus.APPLIED, 10L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCompanyName()).isEqualTo("Acme Corp");
    }

    // ---------- getApplicationById ----------

    @Test
    void getApplicationById_success() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(application));

        ApplicationResponse response = applicationService.getApplicationById(100L, EMAIL);

        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    void getApplicationById_notFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.getApplicationById(100L, EMAIL));
    }

    // ---------- updateApplication ----------

    @Test
    void updateApplication_success_updatesFieldsAndSaves() {
        ApplicationRequest request = new ApplicationRequest();
        request.setCompanyId(10L);
        request.setPositionTitle("Senior Backend Developer");
        request.setAppliedDate(LocalDate.of(2026, 2, 1));
        request.setNotes("Updated notes");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(application));
        when(companyRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.of(company));
        when(applicationRepository.save(application)).thenReturn(application);

        ApplicationResponse response = applicationService.updateApplication(100L, request, EMAIL);

        assertThat(application.getPositionTitle()).isEqualTo("Senior Backend Developer");
        assertThat(application.getNotes()).isEqualTo("Updated notes");
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    void updateApplication_applicationNotFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.updateApplication(100L, new ApplicationRequest(), EMAIL));
    }

    @Test
    void updateApplication_companyNotFound_throws() {
        ApplicationRequest request = new ApplicationRequest();
        request.setCompanyId(999L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(application));
        when(companyRepository.findByIdAndOwner(999L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.updateApplication(100L, request, EMAIL));

        verify(applicationRepository, never()).save(any());
    }

    // ---------- updateStatus ----------

    @Test
    void updateStatus_success_updatesStatusAndSavesHistory() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(ApplicationStatus.INTERVIEW);
        request.setNote("Recruiter call scheduled");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);

        ApplicationResponse response = applicationService.updateStatus(100L, request, EMAIL);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.INTERVIEW);
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.INTERVIEW);

        ArgumentCaptor<StatusHistory> historyCaptor = ArgumentCaptor.forClass(StatusHistory.class);
        verify(statusHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getNote()).isEqualTo("Recruiter call scheduled");
    }

    @Test
    void updateStatus_applicationNotFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.updateStatus(100L, new StatusUpdateRequest(), EMAIL));
    }

    // ---------- getStatusHistory ----------

    @Test
    void getStatusHistory_returnsMappedList() {
        StatusHistory history = new StatusHistory();
        history.setId(1L);
        history.setStatus(ApplicationStatus.APPLIED);
        history.setChangedAt(LocalDateTime.of(2026, 1, 10, 9, 0));
        history.setNote("Başvuru oluşturuldu");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(application));
        when(statusHistoryRepository.findByApplicationOrderByChangedAtAsc(application))
                .thenReturn(List.of(history));

        List<StatusHistoryResponse> result = applicationService.getStatusHistory(100L, EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNote()).isEqualTo("Başvuru oluşturuldu");
    }

    @Test
    void getStatusHistory_applicationNotFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.getStatusHistory(100L, EMAIL));
    }

    // ---------- deleteApplication ----------

    @Test
    void deleteApplication_success_deletesHistoryThenApplication() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(application));

        applicationService.deleteApplication(100L, EMAIL);

        verify(statusHistoryRepository).deleteByApplication(application);
        verify(applicationRepository).delete(application);
    }

    @Test
    void deleteApplication_notFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(applicationRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.deleteApplication(100L, EMAIL));

        verifyNoInteractions(statusHistoryRepository);
        verify(applicationRepository, never()).delete(any(Application.class));
    }
}