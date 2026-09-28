package com.recruitment.platform.repository;

import com.recruitment.platform.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCandidateIdOrderByStartDateDesc(Long candidateId);
    Optional<Project> findByIdAndCandidateId(Long id, Long candidateId);
}
