package com.recruitment.platform.service;

import com.recruitment.platform.dto.CandidateSearchCriteriaDto;
import com.recruitment.platform.dto.CandidateSummaryDto;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.Role;
import com.recruitment.platform.model.User;
import com.recruitment.platform.repository.CandidateRepository;
import com.recruitment.platform.repository.UserRepository;
import com.recruitment.platform.repository.specification.CandidateSpecifications;
import com.recruitment.platform.service.ai.EmbeddingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class CandidateSearchService {

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final EmbeddingService embeddingService;
    private final HybridJobMatchingService hybridJobMatchingService;

    public CandidateSearchService(CandidateRepository candidateRepository,
                                  UserRepository userRepository,
                                  EmbeddingService embeddingService,
                                  HybridJobMatchingService hybridJobMatchingService) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.embeddingService = embeddingService;
        this.hybridJobMatchingService = hybridJobMatchingService;
    }

    /**
     * Executes role-authorized candidate search with multi-criteria filtering, sorting, pagination,
     * and optional dense semantic vector retrieval.
     */
    public Page<CandidateSummaryDto> searchCandidates(CandidateSearchCriteriaDto criteria,
                                                      Pageable pageable,
                                                      String callerEmail) {
        verifyRecruiterOrAdminAccess(callerEmail);

        Specification<Candidate> spec = CandidateSpecifications.withCriteria(criteria);

        // Path A: Standard SQL Filtered & Paginated Search
        if (criteria == null || criteria.getSemanticQuery() == null || criteria.getSemanticQuery().trim().isEmpty()) {
            Page<Candidate> candidates = candidateRepository.findAll(spec, pageable);
            return candidates.map(CandidateSummaryDto::fromEntity);
        }

        // Path B: Semantic Vector Search & Ranking
        List<Candidate> candidates = candidateRepository.findAll(spec);
        if (candidates.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        float[] queryVector = embeddingService.generateEmbedding(criteria.getSemanticQuery());

        // Score candidates by cosine similarity
        List<CandidateScored> scoredList = new ArrayList<>();
        for (Candidate c : candidates) {
            float[] cVector = hybridJobMatchingService.getCandidateEmbedding(c);
            double sim = embeddingService.computeCosineSimilarity(queryVector, cVector);
            scoredList.add(new CandidateScored(c, sim));
        }

        // Sort descending by semantic similarity
        scoredList.sort((a, b) -> Double.compare(b.similarity, a.similarity));

        // In-memory pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), scoredList.size());
        List<CandidateSummaryDto> pageContent = new ArrayList<>();
        if (start < scoredList.size()) {
            for (int i = start; i < end; i++) {
                pageContent.add(CandidateSummaryDto.fromEntity(scoredList.get(i).candidate));
            }
        }

        return new PageImpl<>(pageContent, pageable, scoredList.size());
    }

    private void verifyRecruiterOrAdminAccess(String callerEmail) {
        if (callerEmail == null || callerEmail.isBlank()) {
            throw new AccessDeniedException("Authentication required to search candidate database.");
        }

        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new AccessDeniedException("User account not found: " + callerEmail));

        if (user.getRole() != Role.RECRUITER && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: Candidate database search is restricted to Recruiters and Administrators.");
        }
    }

    private static class CandidateScored {
        final Candidate candidate;
        final double similarity;

        CandidateScored(Candidate candidate, double similarity) {
            this.candidate = candidate;
            this.similarity = similarity;
        }
    }
}
