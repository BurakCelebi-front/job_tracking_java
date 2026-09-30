package com.example.job_tracking.controller;


import com.example.job_tracking.dto.ApplicationRequest;
import com.example.job_tracking.dto.ApplicationResponse;
import com.example.job_tracking.dto.StatusHistoryResponse;
import com.example.job_tracking.dto.StatusUpdateRequest;
import com.example.job_tracking.entity.ApplicationStatus;
import com.example.job_tracking.security.JwtAuthenticationFilter;
import com.example.job_tracking.service.ApplicationService;
import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ApplicationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@WithMockUser(username = "daria@example.com")
class ApplicationControllerTest {

    private static final String EMAIL = "daria@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private ApplicationService applicationService;

    private ApplicationResponse sampleResponse() {
        return new ApplicationResponse(100L, 10L, "Acme Corp", "Backend Developer",
                ApplicationStatus.APPLIED, LocalDate.of(2026, 1, 10), "Referral");
    }

    // ---------- POST /api/applications ----------

    @Test
    void createApplication_validRequest_returns200WithBody() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setCompanyId(10L);
        request.setPositionTitle("Backend Developer");
        request.setAppliedDate(LocalDate.of(2026, 1, 10));
        request.setNotes("Referral");

        when(applicationService.createApplication(any(ApplicationRequest.class), eq(EMAIL)))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.companyName").value("Acme Corp"));
    }

    @Test
    void createApplication_missingCompanyId_returns400AndDoesNotCallService() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setPositionTitle("Backend Developer");
        // companyId boş bırakıldı

        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(applicationService);
    }

    // ---------- GET /api/applications ----------

    @Test
    void getMyApplications_returnsListFromService() throws Exception {
        when(applicationService.getMyApplications(EMAIL)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].positionName").value("Backend Developer"));
    }

    // ---------- GET /api/applications/{id} ----------

    @Test
    void getApplicationById_returnsBody() throws Exception {
        when(applicationService.getApplicationById(100L, EMAIL)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/applications/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    // ---------- GET /api/applications/search ----------

    @Test
    @SuppressWarnings("unchecked")
    void searchApplications_returnsPageJson() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ApplicationResponse> page = new PageImpl<>(List.of(sampleResponse()), pageable, 1);

        when(applicationService.searchApplications(eq(EMAIL), eq(ApplicationStatus.APPLIED), eq(10L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/applications/search")
                        .param("status", "APPLIED")
                        .param("companyId", "10")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ---------- PUT /api/applications/{id} ----------

    @Test
    void updateApplication_validRequest_returns200() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setCompanyId(10L);
        request.setPositionTitle("Senior Backend Developer");
        request.setAppliedDate(LocalDate.of(2026, 2, 1));
        request.setNotes("Updated");

        when(applicationService.updateApplication(eq(100L), any(ApplicationRequest.class), eq(EMAIL)))
                .thenReturn(sampleResponse());

        mockMvc.perform(put("/api/applications/100")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void updateApplication_missingCompanyId_returns400AndDoesNotCallService() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setPositionTitle("Senior Backend Developer");
        // companyId boş bırakıldı

        mockMvc.perform(put("/api/applications/100")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(applicationService);
    }

    // ---------- PATCH /api/applications/{id}/status ----------

    @Test
    void updateStatus_validRequest_returns200() throws Exception {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(ApplicationStatus.INTERVIEW);
        request.setNote("Recruiter call scheduled");

        when(applicationService.updateStatus(eq(100L), any(StatusUpdateRequest.class), eq(EMAIL)))
                .thenReturn(sampleResponse());

        mockMvc.perform(patch("/api/applications/100/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void updateStatus_missingStatus_returns400AndDoesNotCallService() throws Exception {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNote("Recruiter call scheduled");
        // status boş bırakıldı

        mockMvc.perform(patch("/api/applications/100/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(applicationService);
    }

    // ---------- GET /api/applications/{id}/status-history ----------

    @Test
    void getStatusHistory_returnsList() throws Exception {
        StatusHistoryResponse history = new StatusHistoryResponse(1L, ApplicationStatus.APPLIED,
                java.time.LocalDateTime.of(2026, 1, 10, 9, 0), "The application has been created");

        when(applicationService.getStatusHistory(100L, EMAIL)).thenReturn(List.of(history));

        mockMvc.perform(get("/api/applications/100/status-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].note").value("The application has been created"));
    }

    // ---------- DELETE /api/applications/{id} ----------

    @Test
    void deleteApplication_returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/applications/100")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(applicationService).deleteApplication(100L, EMAIL);
    }
}