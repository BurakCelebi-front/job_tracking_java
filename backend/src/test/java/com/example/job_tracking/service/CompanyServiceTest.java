package com.example.job_tracking.service;

/*
 * Varsayımlar (kaynağı görmediğim yerler):
 * - CompanyRequest: no-arg constructor + setName/setWebsite/setLocation setter'ları
 * - Company entity: setId, setName, setWebsite, setLocation, setOwner (ApplicationServiceTest'teki gibi)
 * - CompanyResponse ctor sırası: (id, name, website, location) — toResponse()'tan birebir
 */

import com.example.job_tracking.dto.CompanyRequest;
import com.example.job_tracking.dto.CompanyResponse;
import com.example.job_tracking.entity.Application;
import com.example.job_tracking.entity.Company;
import com.example.job_tracking.entity.User;
import com.example.job_tracking.repository.ApplicationRepository;
import com.example.job_tracking.repository.CompanyRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationRepository applicationRepository;

    @InjectMocks
    private CompanyService companyService;

    private static final String EMAIL = "daria@example.com";

    private User user;
    private Company company;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);

        company = new Company();
        company.setId(10L);
        company.setName("Acme Corp");
        company.setWebsite("https://acme.example");
        company.setLocation("Remote");
        company.setOwner(user);
    }

    // ---------- createCompany ----------

    @Test
    void createCompany_success_savesAndReturnsResponse() {
        CompanyRequest request = new CompanyRequest();
        request.setName("Acme Corp");
        request.setWebsite("https://acme.example");
        request.setLocation("Remote");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        CompanyResponse response = companyService.createCompany(request, EMAIL);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Acme Corp");
        assertThat(response.getWebsite()).isEqualTo("https://acme.example");
        assertThat(response.getLocation()).isEqualTo("Remote");

        ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isEqualTo(user);
    }

    @Test
    void createCompany_userNotFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> companyService.createCompany(new CompanyRequest(), EMAIL));

        verifyNoInteractions(companyRepository);
    }

    // ---------- getMyCompanies ----------

    @Test
    void getMyCompanies_returnsMappedList() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByOwner(user)).thenReturn(List.of(company));

        List<CompanyResponse> result = companyService.getMyCompanies(EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Acme Corp");
    }

    @Test
    void getMyCompanies_userNotFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> companyService.getMyCompanies(EMAIL));
    }

    // ---------- getCompanyById ----------

    @Test
    void getCompanyById_success() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.of(company));

        CompanyResponse response = companyService.getCompanyById(10L, EMAIL);

        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    void getCompanyById_notFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> companyService.getCompanyById(10L, EMAIL));
    }

    // ---------- updateCompany ----------

    @Test
    void updateCompany_success_updatesFieldsAndSaves() {
        CompanyRequest request = new CompanyRequest();
        request.setName("Acme Corp Updated");
        request.setWebsite("https://acme-updated.example");
        request.setLocation("Istanbul");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);

        CompanyResponse response = companyService.updateCompany(10L, request, EMAIL);

        assertThat(company.getName()).isEqualTo("Acme Corp Updated");
        assertThat(company.getLocation()).isEqualTo("Istanbul");
        assertThat(response.getName()).isEqualTo("Acme Corp Updated");
    }

    @Test
    void updateCompany_notFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> companyService.updateCompany(10L, new CompanyRequest(), EMAIL));

        verify(companyRepository, never()).save(any());
    }

    // ---------- searchCompanies ----------

    @Test
    void searchCompanies_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Company> page = new PageImpl<>(List.of(company), pageable, 1);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<CompanyResponse> result = companyService.searchCompanies(EMAIL, "Acme", "Remote", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Acme Corp");
    }

    // ---------- deleteCompany ----------

    @Test
    void deleteCompany_success_noApplications() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.of(company));
        when(applicationRepository.findByCompany(company)).thenReturn(Collections.emptyList());

        companyService.deleteCompany(10L, EMAIL);

        verify(companyRepository).delete(company);
    }

    @Test
    void deleteCompany_hasAssociatedApplications_throwsAndDoesNotDelete() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.of(company));
        when(applicationRepository.findByCompany(company)).thenReturn(List.of(new Application()));

        assertThrows(RuntimeException.class,
                () -> companyService.deleteCompany(10L, EMAIL));

        verify(companyRepository, never()).delete(any(Company.class));
    }

    @Test
    void deleteCompany_notFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> companyService.deleteCompany(10L, EMAIL));

        verifyNoInteractions(applicationRepository);
        verify(companyRepository, never()).delete(any(Company.class));
    }
}