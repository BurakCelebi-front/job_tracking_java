package com.example.job_tracking.specification;

import com.example.job_tracking.entity.Company;
import com.example.job_tracking.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class CompanySpecification {
    public static Specification<Company> hasOwner(User owner) {
        return (root, query, cb) -> cb.equal(root.get("owner"), owner);
    }

    public static Specification<Company> nameContains(String name) {
        return (root, query, cb) -> (name == null || name.isBlank()) 
        ? null
        : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Company> locationContains(String location) {
        return (root, query, cb) -> (location == null || location.isBlank())
        ? null
        : cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }
    
}
