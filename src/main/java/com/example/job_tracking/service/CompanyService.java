package com.example.job_tracking.service;

import com.example.job_tracking.dto.CompanyRequest;
import com.example.job_tracking.dto.CompanyResponse;
import com.example.job_tracking.entity.Company;
import com.example.job_tracking.entity.User;
import com.example.job_tracking.repository.CompanyRepository;
import com.example.job_tracking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository){
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private CompanyResponse toResponse(Company company) {
        return new CompanyResponse(company.getId(), company.getName(), company.getWebsite(), company.getLocation());
    }

    public CompanyResponse createCompany(CompanyRequest request, String userEmail){
        User owner = getUserByEmail(userEmail);

        Company company = new Company();
        company.setName(request.getName());
        company.setWebsite(request.getWebsite());
        company.setLocation(request.getLocation());
        company.setOwner(owner);

        return toResponse(companyRepository.save(company));
    }

    public List<CompanyResponse> getMyCompanies(String userEmail){
        User owner = getUserByEmail(userEmail);
        return companyRepository.findByOwner(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CompanyResponse getCompanyById(Long id, String userEmail) {
        User owner = getUserByEmail(userEmail);
        Company company = companyRepository.findByIdAndOwner(id, owner)
            .orElseThrow(() -> new RuntimeException("The company could not be found, or you do not have access"));
        return toResponse(company);
    }

    public CompanyResponse updateCompany(Long id, CompanyRequest request, String userEmail) {
        User owner = getUserByEmail(userEmail);
        Company company = companyRepository.findByIdAndOwner(id, owner)
            .orElseThrow(() -> new RuntimeException("The company could not be found, or you do not have access"));

        company.setName(request.getName());
        company.setWebsite(request.getWebsite());
        company.setLocation(request.getLocation());

        return  toResponse(companyRepository.save(company));
    }

    public void deleteCompany(Long id, String userEmail){
        User owner = getUserByEmail(userEmail);
        Company company = companyRepository.findByIdAndOwner(id, owner)
            .orElseThrow(() -> new RuntimeException("The company could not be found, or you do not have access"));

            companyRepository.delete(company);
    }

}
