package com.recruitment.platform.controller;

import com.recruitment.platform.dto.InterviewPrepDto;
import com.recruitment.platform.dto.InterviewResponseDto;
import com.recruitment.platform.service.InterviewService;
import com.recruitment.platform.service.ai.AiInterviewAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/interviews")
@Tag(name = "AI Interview Assistant", description = "Generates Technical, Behavioral, Role-Specific Questions and Prep Topics")
public class InterviewPrepController {

    private final AiInterviewAssistantService prepService;
    private final InterviewService interviewService;

    public InterviewPrepController(AiInterviewAssistantService prepService,
                                   InterviewService interviewService) {
        this.prepService = prepService;
        this.interviewService = interviewService;
    }

    @GetMapping("/prep/{jobId}/{candidateId}")
    @Operation(summary = "Generate customized AI interview prep materials based on candidate profile and job requirements")
    public ResponseEntity<InterviewPrepDto> getInterviewPrep(
            @PathVariable Long jobId,
            @PathVariable Long candidateId) {
        InterviewPrepDto prep = prepService.generateInterviewPrep(jobId, candidateId);
        return ResponseEntity.ok(prep);
    }

    @GetMapping("/{interviewId}/prep")
    @Operation(summary = "Generate customized AI interview prep for a scheduled interview appointment")
    public ResponseEntity<InterviewPrepDto> getPrepForInterview(
            @PathVariable Long interviewId,
            Principal principal) {
        String callerEmail = principal != null ? principal.getName() : null;
        InterviewResponseDto interview = interviewService.getInterviewById(interviewId, callerEmail);
        InterviewPrepDto prep = prepService.generateInterviewPrep(interview.getJobId(), interview.getCandidateId());
        return ResponseEntity.ok(prep);
    }
}
