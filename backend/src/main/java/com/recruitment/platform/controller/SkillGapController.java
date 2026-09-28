package com.recruitment.platform.controller;

import com.recruitment.platform.dto.SkillGapAnalysisDto;
import com.recruitment.platform.service.SkillGapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/skill-gap")
@Tag(name = "Skill Gap Analysis", description = "In-depth candidate vs. job qualification comparison and preparation guidance")
public class SkillGapController {

    private final SkillGapService skillGapService;

    public SkillGapController(SkillGapService skillGapService) {
        this.skillGapService = skillGapService;
    }

    @GetMapping("/job/{jobId}/candidate/{candidateId}")
    @Operation(summary = "Perform in-depth skill-gap analysis identifying present, less-evident, and missing skills with an actionable learning plan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detailed skill gap analysis with readiness score and prep recommendations"),
            @ApiResponse(responseCode = "404", description = "Job requisition or Candidate profile not found")
    })
    public ResponseEntity<SkillGapAnalysisDto> getSkillGapAnalysis(
            @PathVariable Long jobId,
            @PathVariable Long candidateId) {
        return ResponseEntity.ok(skillGapService.analyzeSkillGap(jobId, candidateId));
    }
}
