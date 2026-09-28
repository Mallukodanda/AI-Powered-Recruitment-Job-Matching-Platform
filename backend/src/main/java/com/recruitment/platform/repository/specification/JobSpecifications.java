package com.recruitment.platform.repository.specification;

import com.recruitment.platform.dto.JobSearchCriteriaDto;
import com.recruitment.platform.model.Job;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class JobSpecifications {

    public static Specification<Job> withCriteria(JobSearchCriteriaDto criteria) {
        return (root, query, cb) -> {
            if (criteria == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            // 1. Text keyword search across title, skills, description, and requirements
            if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
                String pattern = "%" + criteria.getQuery().trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title").as(String.class)), pattern);
                Predicate skillsMatch = cb.and(cb.isNotNull(root.get("skills")), cb.like(cb.lower(root.get("skills").as(String.class)), pattern));
                Predicate descMatch = cb.and(cb.isNotNull(root.get("description")), cb.like(cb.lower(root.get("description").as(String.class)), pattern));
                Predicate reqMatch = cb.and(cb.isNotNull(root.get("requirements")), cb.like(cb.lower(root.get("requirements").as(String.class)), pattern));
                predicates.add(cb.or(titleMatch, skillsMatch, descMatch, reqMatch));
            }

            // 2. Department filter
            if (criteria.getDepartment() != null && !criteria.getDepartment().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("department").as(String.class)), criteria.getDepartment().trim().toLowerCase()));
            }

            // 3. Location filter (matches city or Remote)
            if (criteria.getLocation() != null && !criteria.getLocation().trim().isEmpty()) {
                String locPattern = "%" + criteria.getLocation().trim().toLowerCase() + "%";
                predicates.add(cb.and(cb.isNotNull(root.get("location")), cb.like(cb.lower(root.get("location").as(String.class)), locPattern)));
            }

            // 4. Job type filter (e.g. FULL_TIME, CONTRACT)
            if (criteria.getJobType() != null && !criteria.getJobType().trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("jobType")), criteria.getJobType().trim().toUpperCase()));
            }

            // 5. Experience level filter (e.g. ENTRY, MID, SENIOR)
            if (criteria.getExperienceLevel() != null && !criteria.getExperienceLevel().trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("experienceLevel")), criteria.getExperienceLevel().trim().toUpperCase()));
            }

            // 6. Experience years range
            if (criteria.getMinExperienceYears() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minExperienceYears"), criteria.getMinExperienceYears()));
            }
            if (criteria.getMaxExperienceYears() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("minExperienceYears"), criteria.getMaxExperienceYears()));
            }

            // 7. Status filter (defaults to ACTIVE)
            if (criteria.getStatus() != null && !criteria.getStatus().trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), criteria.getStatus().trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
