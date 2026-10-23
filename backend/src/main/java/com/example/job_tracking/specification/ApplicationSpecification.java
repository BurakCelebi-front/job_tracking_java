package com.example.job_tracking.specification;

import com.example.job_tracking.entity.Application;
import com.example.job_tracking.entity.ApplicationStatus;
import com.example.job_tracking.entity.User;
import org.springframework.data.jpa.domain.Specification;


public class ApplicationSpecification {
    
    public static Specification<Application> hasUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    public static Specification<Application> hasStatus(ApplicationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Application> hasCompanyId(Long companyId) {
        return (root, query, cb) -> companyId == null ? null : cb.equal(root.get("company").get("id"), companyId);
    }
}
