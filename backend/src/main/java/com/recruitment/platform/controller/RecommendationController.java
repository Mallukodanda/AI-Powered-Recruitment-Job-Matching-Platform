package com.recruitment.platform.controller;

import com.recruitment.platform.dto.CandidateRecommendationDto;
import com.recruitment.platform.dto.JobRecommendationDto;
import com.recruitment.platform.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendations", description = "AI and multi-signal recommendations for candidates and recruiters")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/jobs/{candidateId}")
    @Operation(summary = "Get tailored job recommendations for a candidate based on skills, experience, and location preferences")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranked list of job recommendations with explainable factors"),
            @ApiResponse(responseCode = "403", description = "Unauthorized: Candidates can only view their own recommendations"),
            @ApiResponse(responseCode = "404", description = "Candidate profile not found")
    })
    public ResponseEntity<List<JobRecommendationDto>> getJobRecommendations(
            @PathVariable Long candidateId,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(recommendationService.getJobRecommendations(candidateId, limit, email));
    }

    @GetMapping("/candidates/{jobId}")
    @Operation(summary = "Get qualified candidate recommendations for an open job requisition (Recruiter/Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranked candidate recommendations with evidence and missing skills"),
            @ApiResponse(responseCode = "403", description = "Unauthorized: Requires RECRUITER or ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Job requisition not found")
    })
    public ResponseEntity<List<CandidateRecommendationDto>> getCandidateRecommendations(
            @PathVariable Long jobId,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(recommendationService.getCandidateRecommendations(jobId, limit, email));
    }
}
