package com.tihuz.application_service.specification;

import com.tihuz.application_service.entity.Application;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {

    public static Specification<Application> filter(Boolean isDeleted, String userName, String jobTitle) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isDeleted != null) {
                predicates.add(cb.equal(root.get("isDeleted"), isDeleted));
            }
            if (userName != null && !userName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("userName")), "%" + userName.toLowerCase() + "%"));
            }
            if (jobTitle != null && !jobTitle.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("jobTitle")), "%" + jobTitle.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}