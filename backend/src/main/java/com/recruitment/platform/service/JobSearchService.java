package com.recruitment.platform.service;

import com.recruitment.platform.dto.JobSearchCriteriaDto;
import com.recruitment.platform.dto.JobSearchResultDto;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.repository.JobRepository;
import com.recruitment.platform.repository.specification.JobSpecifications;
import com.recruitment.platform.service.ai.EmbeddingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class JobSearchService {

    private final JobRepository jobRepository;
    private final EmbeddingService embeddingService;
    private final HybridJobMatchingService hybridJobMatchingService;

    public JobSearchService(JobRepository jobRepository,
                            EmbeddingService embeddingService,
                            HybridJobMatchingService hybridJobMatchingService) {
        this.jobRepository = jobRepository;
        this.embeddingService = embeddingService;
        this.hybridJobMatchingService = hybridJobMatchingService;
    }

    /**
     * Executes advanced filtered, sorted, paginated search with optional semantic similarity reranking.
     */
    public Page<JobSearchResultDto> searchJobs(JobSearchCriteriaDto criteria, Pageable pageable) {
        Specification<Job> spec = JobSpecifications.withCriteria(criteria);

        // Path A: Standard Filtered + Paginated SQL Search (No semantic reranking requested)
        if (criteria == null || criteria.getSemanticQuery() == null || criteria.getSemanticQuery().trim().isEmpty()) {
            Page<Job> jobPage = jobRepository.findAll(spec, pageable);
            return jobPage.map(job -> new JobSearchResultDto(job, null, extractHighlights(job, criteria)));
        }

        // Path B: Hybrid Search - Filter in DB, then Dense Semantic Vector Reranking
        List<Job> matchedJobs = jobRepository.findAll(spec);
        if (matchedJobs.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        float[] queryVector = embeddingService.generateEmbedding(criteria.getSemanticQuery());

        List<JobSearchResultDto> scoredResults = new ArrayList<>();
        for (Job job : matchedJobs) {
            float[] jobVector = hybridJobMatchingService.getJobEmbedding(job);
            double similarity = embeddingService.computeCosineSimilarity(queryVector, jobVector) * 100.0;
            double clampedScore = Math.max(0.0, Math.min(100.0, Math.round(similarity * 10.0) / 10.0));

            scoredResults.add(new JobSearchResultDto(job, clampedScore, extractHighlights(job, criteria)));
        }

        // Sort descending by semantic relevance score
        scoredResults.sort((a, b) -> Double.compare(
                b.getSemanticRelevanceScore() != null ? b.getSemanticRelevanceScore() : 0.0,
                a.getSemanticRelevanceScore() != null ? a.getSemanticRelevanceScore() : 0.0
        ));

        // In-memory pagination of scored results
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), scoredResults.size());
        List<JobSearchResultDto> pageContent = (start <= scoredResults.size())
                ? scoredResults.subList(start, end)
                : Collections.emptyList();

        return new PageImpl<>(pageContent, pageable, scoredResults.size());
    }

    private List<String> extractHighlights(Job job, JobSearchCriteriaDto criteria) {
        List<String> highlights = new ArrayList<>();
        if (criteria == null) return highlights;

        if (criteria.getDepartment() != null && criteria.getDepartment().equalsIgnoreCase(job.getDepartment())) {
            highlights.add("Department: " + job.getDepartment());
        }
        if (criteria.getLocation() != null && job.getLocation() != null &&
                job.getLocation().toLowerCase().contains(criteria.getLocation().toLowerCase())) {
            highlights.add("Location: " + job.getLocation());
        }
        if (criteria.getJobType() != null && criteria.getJobType().equalsIgnoreCase(job.getJobType())) {
            highlights.add("Type: " + job.getJobType());
        }
        if (criteria.getMinExperienceYears() != null && job.getMinExperienceYears() != null &&
                job.getMinExperienceYears() >= criteria.getMinExperienceYears()) {
            highlights.add("Experience: " + job.getMinExperienceYears() + " yrs");
        }
        return highlights;
    }
}
