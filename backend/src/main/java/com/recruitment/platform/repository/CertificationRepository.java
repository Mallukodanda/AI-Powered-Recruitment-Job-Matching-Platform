package com.recruitment.platform.repository;

import com.recruitment.platform.model.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByCandidateIdOrderByIssueDateDesc(Long candidateId);
    Optional<Certification> findByIdAndCandidateId(Long id, Long candidateId);
}
