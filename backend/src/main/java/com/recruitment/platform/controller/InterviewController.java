package com.recruitment.platform.controller;

import com.recruitment.platform.dto.InterviewResponseDto;
import com.recruitment.platform.dto.InterviewScheduleRequest;
import com.recruitment.platform.dto.InterviewUpdateRequest;
import com.recruitment.platform.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
@Tag(name = "Interviews", description = "Interview Scheduling, Calendar Management, Feedback, and Multi-Timezone Coordination")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    @Operation(summary = "Schedule a new interview with timezone specification and conflict validation")
    public ResponseEntity<InterviewResponseDto> scheduleInterview(
            @Valid @RequestBody InterviewScheduleRequest request,
            Principal principal) {
        String callerEmail = principal != null ? principal.getName() : null;
        InterviewResponseDto response = interviewService.scheduleInterview(request, callerEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get interview details by ID")
    public ResponseEntity<InterviewResponseDto> getInterviewById(
            @PathVariable Long id,
            Principal principal) {
        String callerEmail = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(interviewService.getInterviewById(id, callerEmail));
    }

    @GetMapping
    @Operation(summary = "List interviews filtered by candidate, job, or all")
    public ResponseEntity<List<InterviewResponseDto>> getInterviews(
            @RequestParam(required = false) Long candidateId,
            @RequestParam(required = false) Long jobId,
            Principal principal) {
        String callerEmail = principal != null ? principal.getName() : null;
        if (candidateId != null) {
            return ResponseEntity.ok(interviewService.getInterviewsForCandidate(candidateId, callerEmail));
        }
        if (jobId != null) {
            return ResponseEntity.ok(interviewService.getInterviewsForJob(jobId, callerEmail));
        }
        return ResponseEntity.ok(interviewService.getAllInterviews(callerEmail));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update interview status, rating, feedback, or reschedule to a new time")
    public ResponseEntity<InterviewResponseDto> updateInterview(
            @PathVariable Long id,
            @RequestBody InterviewUpdateRequest request,
            Principal principal) {
        String callerEmail = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(interviewService.updateInterview(id, request, callerEmail));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a scheduled interview")
    public ResponseEntity<Void> cancelInterview(
            @PathVariable Long id,
            Principal principal) {
        String callerEmail = principal != null ? principal.getName() : null;
        interviewService.cancelInterview(id, callerEmail);
        return ResponseEntity.noContent().build();
    }
}
