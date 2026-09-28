package com.recruitment.platform.repository;

import com.recruitment.platform.model.Application;
import com.recruitment.platform.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByJobIdOrderByAiMatchScoreDesc(Long jobId);
    List<Application> findByCandidateIdOrderByAppliedAtDesc(Long candidateId);
    List<Application> findByStatus(ApplicationStatus status);
    Optional<Application> findByJobIdAndCandidateId(Long jobId, Long candidateId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = :status")
    long countByStatus(ApplicationStatus status);

    @Query("SELECT AVG(a.aiMatchScore) FROM Application a")
    Double getAverageMatchScore();

    @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status")
    List<Object[]> countApplicationsGroupedByStatus();
}
