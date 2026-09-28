package com.recruitment.platform.repository;

import com.recruitment.platform.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByCandidateIdOrderByStartDateDesc(Long candidateId);
    Optional<Experience> findByIdAndCandidateId(Long id, Long candidateId);
}
