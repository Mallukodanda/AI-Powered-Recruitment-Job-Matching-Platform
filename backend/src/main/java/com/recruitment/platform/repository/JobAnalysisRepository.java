package com.recruitment.platform.repository;

import com.recruitment.platform.model.JobAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, Long> {
    Optional<JobAnalysis> findByJobId(Long jobId);
    Optional<JobAnalysis> findByJobIdAndSha256Hash(Long jobId, String sha256Hash);
}
