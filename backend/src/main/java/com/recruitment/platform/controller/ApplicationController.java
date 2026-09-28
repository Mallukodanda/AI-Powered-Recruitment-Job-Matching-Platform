package com.recruitment.platform.controller;

import com.recruitment.platform.model.Application;
import com.recruitment.platform.model.ApplicationStatus;
import com.recruitment.platform.service.ApplicationWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "Applications & Workflow", description = "Hiring Pipeline, Kanban Stages, and Interview Scheduling")
public class ApplicationController {

    private final ApplicationWorkflowService workflowService;

    public ApplicationController(ApplicationWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    @Operation(summary = "List recruitment applications, optionally filtered by job or candidate")
    public ResponseEntity<List<Application>> getApplications(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long candidateId) {
        if (jobId != null) {
            return ResponseEntity.ok(workflowService.getApplicationsByJob(jobId));
        }
        if (candidateId != null) {
            return ResponseEntity.ok(workflowService.getApplicationsByCandidate(candidateId));
        }
        return ResponseEntity.ok(workflowService.getAllApplications());
    }

    @PostMapping
    @Operation(summary = "Submit candidate application with automated AI scoring and auto-shortlisting")
    public ResponseEntity<Application> apply(@RequestBody Map<String, Long> payload) {
        Long jobId = payload.get("jobId");
        Long candidateId = payload.get("candidateId");
        if (jobId == null || candidateId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(workflowService.submitApplication(jobId, candidateId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Advance or transition candidate stage in the recruitment pipeline")
    public ResponseEntity<Application> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String statusStr = payload.get("status");
        String notes = payload.get("notes");
        ApplicationStatus newStatus = ApplicationStatus.valueOf(statusStr.toUpperCase());
        return ResponseEntity.ok(workflowService.updateStatus(id, newStatus, notes));
    }

    @PostMapping("/{id}/interview")
    @Operation(summary = "Schedule interview time and add notes for a candidate")
    public ResponseEntity<Application> scheduleInterview(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime interviewDate,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(workflowService.scheduleInterview(id, interviewDate, notes));
    }
}
