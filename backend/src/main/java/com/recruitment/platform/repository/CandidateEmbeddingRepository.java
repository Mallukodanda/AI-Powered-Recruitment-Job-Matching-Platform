package com.recruitment.platform.repository;

import com.recruitment.platform.model.CandidateEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateEmbeddingRepository extends JpaRepository<CandidateEmbedding, Long> {
    Optional<CandidateEmbedding> findByCandidateId(Long candidateId);
    Optional<CandidateEmbedding> findByCandidateIdAndSha256Hash(Long candidateId, String sha256Hash);
}
