package com.recruitment.platform.repository;

import com.recruitment.platform.model.ResumeAnalysis;
import com.recruitment.platform.model.ResumeAnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    List<ResumeAnalysis> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);
    Optional<ResumeAnalysis> findByIdAndCandidateId(Long id, Long candidateId);
    List<ResumeAnalysis> findByStatus(ResumeAnalysisStatus status);
}
