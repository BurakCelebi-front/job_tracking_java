package com.example.job_tracking.service;

import com.example.job_tracking.entity.Application;
import com.example.job_tracking.dto.CompanyRequest;
import com.example.job_tracking.dto.CompanyResponse;
import com.example.job_tracking.entity.Company;
import com.example.job_tracking.entity.User;
import com.example.job_tracking.repository.CompanyRepository;
import com.example.job_tracking.repository.UserRepository;
import com.example.job_tracking.specification.CompanySpecification;
import com.example.job_tracking.repository.ApplicationRepository;

import com.example.job_tracking.specification.CompanySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository, ApplicationRepository applicationRepository){
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
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

    public Page<CompanyResponse> searchCompanies(String userEmail, String name, String location, Pageable pageable) {
    User user = getUserByEmail(userEmail);

    Specification<Company> spec = Specification
        .where(CompanySpecification.hasOwner(user))
        .and(CompanySpecification.nameContains(name))
        .and(CompanySpecification.locationContains(location));

    Page<Company> companies = companyRepository.findAll(spec, pageable);

    return companies.map(c -> new CompanyResponse(
        c.getId(),
        c.getName(),
        c.getWebsite(),
        c.getLocation()
    ));
}


    public void deleteCompany(Long id, String userEmail){
        User user = getUserByEmail(userEmail);
        Company company = companyRepository.findByIdAndOwner(id, user)
            .orElseThrow(() -> new RuntimeException("The company could not be found, or you do not have access"));

        List<Application> applications = applicationRepository.findByCompany(company);
        if (!applications.isEmpty()) {
            throw new RuntimeException(
                "There are  " + applications.size() + " apps associated with this company. Please delete these apps or transfer them to another company first."
                
            );
        }

            companyRepository.delete(company);
    }

}
