package com.recruitment.platform.repository;

import com.recruitment.platform.model.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long>, JpaSpecificationExecutor<Candidate> {

    Optional<Candidate> findByEmail(String email);

    Optional<Candidate> findByEmailIgnoreCase(String email);

    Optional<Candidate> findByUserId(Long userId);

    @Query("SELECT c FROM Candidate c WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(CAST(c.skillsSummary AS string)) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.currentTitle) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Candidate> searchCandidates(@Param("query") String query);

    @Query("SELECT c FROM Candidate c WHERE (:query IS NULL OR :query = '' " +
           "OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(CAST(c.skillsSummary AS string)) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.currentTitle) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.location) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Candidate> searchCandidatesPaginated(@Param("query") String query, Pageable pageable);
}
