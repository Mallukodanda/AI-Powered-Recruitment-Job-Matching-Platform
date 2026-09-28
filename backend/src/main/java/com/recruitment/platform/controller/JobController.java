package com.recruitment.platform.controller;

import com.recruitment.platform.dto.JobSearchCriteriaDto;
import com.recruitment.platform.dto.JobSearchResultDto;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.service.JobSearchService;
import com.recruitment.platform.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Jobs", description = "Job Openings & Requisition Management")
public class JobController {

    private final JobService jobService;
    private final JobSearchService jobSearchService;

    public JobController(JobService jobService, JobSearchService jobSearchService) {
        this.jobService = jobService;
        this.jobSearchService = jobSearchService;
    }

    @PostMapping("/search")
    @Operation(summary = "Advanced multi-criteria job search with dynamic filtering, sorting, pagination, and semantic relevance")
    public ResponseEntity<Page<JobSearchResultDto>> searchJobs(
            @RequestBody(required = false) JobSearchCriteriaDto criteria,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        JobSearchCriteriaDto searchCriteria = criteria != null ? criteria : new JobSearchCriteriaDto();
        return ResponseEntity.ok(jobSearchService.searchJobs(searchCriteria, pageable));
    }

    @GetMapping
    @Operation(summary = "List all active job openings or search by keyword")
    public ResponseEntity<List<Job>> getJobs(@RequestParam(required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(jobService.searchJobs(search));
        }
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed job specification by ID")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobService.getJobById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new job posting with skill requirements")
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        return ResponseEntity.ok(jobService.createJob(job));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job posting details")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(id, job));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete or archive a job posting")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
