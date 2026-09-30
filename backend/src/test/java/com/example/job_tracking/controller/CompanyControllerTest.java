package com.example.job_tracking.controller;


import com.example.job_tracking.dto.CompanyRequest;
import com.example.job_tracking.dto.CompanyResponse;
import com.example.job_tracking.security.JwtAuthenticationFilter;
import com.example.job_tracking.service.CompanyService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CompanyController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@WithMockUser(username = "burak@example.com")
class CompanyControllerTest {

    private static final String EMAIL = "burak@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private CompanyService companyService;

    private CompanyResponse sampleResponse() {
        return new CompanyResponse(10L, "Acme Corp", "https://acme.example", "Remote");
    }

    // ---------- POST /api/companies ----------

    @Test
    void createCompany_validRequest_returns200WithBody() throws Exception {
        CompanyRequest request = new CompanyRequest();
        request.setName("Acme Corp");
        request.setWebsite("https://acme.example");
        request.setLocation("Remote");

        when(companyService.createCompany(any(CompanyRequest.class), eq(EMAIL)))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Acme Corp"));
    }

    @Test
    void createCompany_missingName_returns400AndDoesNotCallService() throws Exception {
        CompanyRequest request = new CompanyRequest();
        request.setWebsite("https://acme.example");
        // name boş bırakıldı

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    // ---------- GET /api/companies ----------

    @Test
    void getMyCompanies_returnsListFromService() throws Exception {
        when(companyService.getMyCompanies(EMAIL)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Acme Corp"));
    }

    // ---------- GET /api/companies/{id} ----------

    @Test
    void getCompanyById_returnsBody() throws Exception {
        when(companyService.getCompanyById(10L, EMAIL)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/companies/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    // ---------- GET /api/companies/search ----------

    @Test
    void searchCompanies_returnsPageJson() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CompanyResponse> page = new PageImpl<>(List.of(sampleResponse()), pageable, 1);

        when(companyService.searchCompanies(eq(EMAIL), eq("Acme"), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/companies/search")
                        .param("name", "Acme")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Acme Corp"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ---------- PUT /api/companies/{id} ----------

    @Test
    void updateCompany_validRequest_returns200() throws Exception {
        CompanyRequest request = new CompanyRequest();
        request.setName("Acme Corp Updated");
        request.setWebsite("https://acme-updated.example");
        request.setLocation("Istanbul");

        when(companyService.updateCompany(eq(10L), any(CompanyRequest.class), eq(EMAIL)))
                .thenReturn(sampleResponse());

        mockMvc.perform(put("/api/companies/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void updateCompany_missingName_returns400AndDoesNotCallService() throws Exception {
        CompanyRequest request = new CompanyRequest();
        request.setWebsite("https://acme-updated.example");
        // name boş bırakıldı

        mockMvc.perform(put("/api/companies/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    // ---------- DELETE /api/companies/{id} ----------

    @Test
    void deleteCompany_returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/companies/10")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(companyService).deleteCompany(10L, EMAIL);
    }
}