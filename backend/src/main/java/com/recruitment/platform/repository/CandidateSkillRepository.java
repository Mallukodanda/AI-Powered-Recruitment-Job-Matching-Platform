package com.recruitment.platform.repository;

import com.recruitment.platform.model.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {
    List<CandidateSkill> findByCandidateIdOrderByNameAsc(Long candidateId);
    Optional<CandidateSkill> findByIdAndCandidateId(Long id, Long candidateId);
    Optional<CandidateSkill> findByCandidateIdAndNameIgnoreCase(Long candidateId, String name);
    boolean existsByCandidateIdAndNameIgnoreCase(Long candidateId, String name);
}
