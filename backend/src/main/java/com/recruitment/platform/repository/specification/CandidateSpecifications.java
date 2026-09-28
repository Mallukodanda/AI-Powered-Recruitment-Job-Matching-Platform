package com.recruitment.platform.repository.specification;

import com.recruitment.platform.dto.CandidateSearchCriteriaDto;
import com.recruitment.platform.model.Candidate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CandidateSpecifications {

    public static Specification<Candidate> withCriteria(CandidateSearchCriteriaDto criteria) {
        return (root, query, cb) -> {
            if (criteria == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            // 1. Text keyword search across fullName, currentTitle, headline, skillsSummary, and bio
            if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
                String pattern = "%" + criteria.getQuery().trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("fullName").as(String.class)), pattern);
                Predicate titleMatch = cb.and(cb.isNotNull(root.get("currentTitle")), cb.like(cb.lower(root.get("currentTitle").as(String.class)), pattern));
                Predicate headlineMatch = cb.and(cb.isNotNull(root.get("headline")), cb.like(cb.lower(root.get("headline").as(String.class)), pattern));
                Predicate skillsMatch = cb.and(cb.isNotNull(root.get("skillsSummary")), cb.like(cb.lower(root.get("skillsSummary").as(String.class)), pattern));
                Predicate bioMatch = cb.and(cb.isNotNull(root.get("bio")), cb.like(cb.lower(root.get("bio").as(String.class)), pattern));
                predicates.add(cb.or(nameMatch, titleMatch, headlineMatch, skillsMatch, bioMatch));
            }

            // 2. Specific skill filters (candidate skillsSummary must contain each requested skill)
            if (criteria.getSkills() != null && !criteria.getSkills().isEmpty()) {
                for (String skill : criteria.getSkills()) {
                    if (skill != null && !skill.trim().isEmpty()) {
                        String skillPattern = "%" + skill.trim().toLowerCase() + "%";
                        predicates.add(cb.and(cb.isNotNull(root.get("skillsSummary")), cb.like(cb.lower(root.get("skillsSummary").as(String.class)), skillPattern)));
                    }
                }
            }

            // 3. Location filter
            if (criteria.getLocation() != null && !criteria.getLocation().trim().isEmpty()) {
                String locPattern = "%" + criteria.getLocation().trim().toLowerCase() + "%";
                predicates.add(cb.and(cb.isNotNull(root.get("location")), cb.like(cb.lower(root.get("location").as(String.class)), locPattern)));
            }

            // 4. Current job title filter
            if (criteria.getCurrentTitle() != null && !criteria.getCurrentTitle().trim().isEmpty()) {
                String titlePattern = "%" + criteria.getCurrentTitle().trim().toLowerCase() + "%";
                predicates.add(cb.and(cb.isNotNull(root.get("currentTitle")), cb.like(cb.lower(root.get("currentTitle").as(String.class)), titlePattern)));
            }

            // 5. Minimum years of experience
            if (criteria.getMinExperienceYears() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        cb.coalesce(root.get("yearsExperience"), 0),
                        criteria.getMinExperienceYears().intValue()
                ));
            }

            // 6. Education degree level
            if (criteria.getEducationDegree() != null && !criteria.getEducationDegree().trim().isEmpty()) {
                String eduPattern = "%" + criteria.getEducationDegree().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(cb.coalesce(root.get("highestEducation"), "")), eduPattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
