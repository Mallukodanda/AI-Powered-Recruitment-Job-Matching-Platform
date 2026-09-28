package com.recruitment.platform.controller;

import com.recruitment.platform.dto.CandidateRankDto;
import com.recruitment.platform.dto.ExplainableMatchResult;
import com.recruitment.platform.dto.JobMatchRequest;
import com.recruitment.platform.dto.MatchScoreResponse;
import com.recruitment.platform.dto.StructuredJobData;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.service.AiJobMatchingService;
import com.recruitment.platform.service.CandidateService;
import com.recruitment.platform.service.HybridJobMatchingService;
import com.recruitment.platform.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/matching")
@Tag(name = "AI Matching Engine", description = "Semantic Scoring, Dense Embeddings, Candidate Ranking, and Explainable Matching")
public class AiMatchingController {

    private final AiJobMatchingService aiJobMatchingService;
    private final HybridJobMatchingService hybridJobMatchingService;
    private final JobService jobService;
    private final CandidateService candidateService;

    public AiMatchingController(
            AiJobMatchingService aiJobMatchingService,
            HybridJobMatchingService hybridJobMatchingService,
            JobService jobService,
            CandidateService candidateService) {
        this.aiJobMatchingService = aiJobMatchingService;
        this.hybridJobMatchingService = hybridJobMatchingService;
        this.jobService = jobService;
        this.candidateService = candidateService;
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyze match score between a Job and Candidate/Resume text")
    public ResponseEntity<?> analyzeMatch(@RequestBody JobMatchRequest request) {
        if (request.getJobId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "jobId is required"));
        }

        Job job = jobService.getJobById(request.getJobId()).orElse(null);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        Candidate candidate;
        if (request.getCandidateId() != null) {
            candidate = candidateService.getCandidateById(request.getCandidateId()).orElse(null);
            if (candidate == null) {
                return ResponseEntity.notFound().build();
            }
        } else if (request.getCustomResumeText() != null && !request.getCustomResumeText().isBlank()) {
            candidate = candidateService.processResumeText(request.getCustomResumeText(), "Analyzed Candidate", "temp@review.ai");
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Either candidateId or customResumeText must be provided"));
        }

        MatchScoreResponse matchResult = aiJobMatchingService.calculateMatch(job, candidate);
        return ResponseEntity.ok(matchResult);
    }

    @PostMapping("/compare")
    @Operation(summary = "Perform explainable hybrid comparison between a Job and Candidate")
    public ResponseEntity<?> compareCandidateToJob(@RequestBody JobMatchRequest request) {
        if (request.getJobId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "jobId is required"));
        }

        Job job = jobService.getJobById(request.getJobId()).orElse(null);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        Candidate candidate;
        if (request.getCandidateId() != null) {
            candidate = candidateService.getCandidateById(request.getCandidateId()).orElse(null);
            if (candidate == null) {
                return ResponseEntity.notFound().build();
            }
        } else if (request.getCustomResumeText() != null && !request.getCustomResumeText().isBlank()) {
            candidate = candidateService.processResumeText(request.getCustomResumeText(), "Analyzed Candidate", "temp@review.ai");
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Either candidateId or customResumeText must be provided"));
        }

        ExplainableMatchResult result = hybridJobMatchingService.match(job, candidate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/explain/{jobId}/{candidateId}")
    @Operation(summary = "Get explainable evidence-based matching breakdown for candidate and job")
    public ResponseEntity<?> getExplainableMatch(@PathVariable Long jobId, @PathVariable Long candidateId) {
        Job job = jobService.getJobById(jobId).orElse(null);
        if (job == null) return ResponseEntity.notFound().build();

        Candidate candidate = candidateService.getCandidateById(candidateId).orElse(null);
        if (candidate == null) return ResponseEntity.notFound().build();

        ExplainableMatchResult result = hybridJobMatchingService.match(job, candidate);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/analyze-job/{jobId}")
    @Operation(summary = "Analyze and extract structured entities from a job description")
    public ResponseEntity<?> analyzeJobDescription(@PathVariable Long jobId) {
        Job job = jobService.getJobById(jobId).orElse(null);
        if (job == null) return ResponseEntity.notFound().build();

        StructuredJobData structuredData = hybridJobMatchingService.getStructuredJobData(job);
        return ResponseEntity.ok(structuredData);
    }

    @GetMapping("/rank-candidates/{jobId}")
    @Operation(summary = "Generate AI-ranked candidate leaderboard for a specific job")
    public ResponseEntity<?> rankCandidatesForJob(@PathVariable Long jobId) {
        Job job = jobService.getJobById(jobId).orElse(null);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        List<Candidate> candidates = candidateService.getAllCandidates();
        List<CandidateRankDto> rankedList = aiJobMatchingService.rankCandidatesForJob(job, candidates);

        return ResponseEntity.ok(rankedList);
    }

    @GetMapping("/recommend-jobs/{candidateId}")
    @Operation(summary = "Recommend best matching jobs for a candidate profile")
    public ResponseEntity<?> recommendJobsForCandidate(@PathVariable Long candidateId) {
        Candidate candidate = candidateService.getCandidateById(candidateId).orElse(null);
        if (candidate == null) {
            return ResponseEntity.notFound().build();
        }

        List<Job> jobs = jobService.getActiveJobs();
        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (Job job : jobs) {
            MatchScoreResponse score = aiJobMatchingService.calculateMatch(job, candidate);
            Map<String, Object> item = new HashMap<>();
            item.put("job", job);
            item.put("matchDetails", score);
            recommendations.add(item);
        }

        recommendations.sort((a, b) -> {
            MatchScoreResponse s1 = (MatchScoreResponse) a.get("matchDetails");
            MatchScoreResponse s2 = (MatchScoreResponse) b.get("matchDetails");
            return Double.compare(s2.getOverallScore(), s1.getOverallScore());
        });

        return ResponseEntity.ok(recommendations);
    }
}
