package com.recruitment.platform.service;

import com.recruitment.platform.dto.DashboardStatsDto;
import com.recruitment.platform.dto.MatchScoreResponse;
import com.recruitment.platform.model.Application;
import com.recruitment.platform.model.ApplicationStatus;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.repository.ApplicationRepository;
import com.recruitment.platform.repository.CandidateRepository;
import com.recruitment.platform.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class ApplicationWorkflowService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final AiJobMatchingService aiJobMatchingService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public ApplicationWorkflowService(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            CandidateRepository candidateRepository,
            AiJobMatchingService aiJobMatchingService,
            org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.aiJobMatchingService = aiJobMatchingService;
        this.eventPublisher = eventPublisher;
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<Application> getApplicationsByJob(Long jobId) {
        return applicationRepository.findByJobIdOrderByAiMatchScoreDesc(jobId);
    }

    public List<Application> getApplicationsByCandidate(Long candidateId) {
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidateId);
    }

    /**
     * Submit an application, automatically compute AI match score, and auto-screen
     */
    public Application submitApplication(Long jobId, Long candidateId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new com.recruitment.platform.exception.ResourceNotFoundException("Job not found: " + jobId));
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new com.recruitment.platform.exception.ResourceNotFoundException("Candidate not found: " + candidateId));

        // Check if existing
        Optional<Application> existing = applicationRepository.findByJobIdAndCandidateId(jobId, candidateId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Run AI Matching
        MatchScoreResponse match = aiJobMatchingService.calculateMatch(job, candidate);

        Application application = new Application();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setAiMatchScore(match.getOverallScore());
        application.setAiRationale(match.getRecommendationRationale());

        // Automated Workflow Trigger: If score >= 75%, auto-shortlist
        if (match.getOverallScore() >= 75.0) {
            application.setStatus(ApplicationStatus.SHORTLISTED);
            application.setRecruiterNotes("Auto-shortlisted by AI Intelligence System (Score: " + match.getOverallScore() + "%)");
        } else {
            application.setStatus(ApplicationStatus.AI_SCREENED);
            application.setRecruiterNotes("Automated screening completed. Under recruiter review.");
        }

        Application saved = applicationRepository.save(application);

        // Publish Asynchronous Domain Event
        eventPublisher.publishEvent(new com.recruitment.platform.event.ApplicationStatusChangedEvent(
                saved.getId(),
                candidate.getId(),
                candidate.getEmail(),
                candidate.getFullName(),
                job.getId(),
                job.getTitle(),
                ApplicationStatus.APPLIED,
                saved.getStatus(),
                saved.getRecruiterNotes()
        ));

        return saved;
    }

    /**
     * Transition application to a new stage in the recruitment pipeline with state-machine validation.
     */
    public Application updateStatus(Long applicationId, ApplicationStatus newStatus, String notes) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new com.recruitment.platform.exception.ResourceNotFoundException("Application not found: " + applicationId));

        ApplicationStatus currentStatus = application.getStatus();

        if (currentStatus.isTerminal()) {
            throw new com.recruitment.platform.exception.InvalidStateTransitionException(
                    "Cannot transition from terminal state " + currentStatus + " for application id " + applicationId);
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new com.recruitment.platform.exception.InvalidStateTransitionException(currentStatus, newStatus);
        }

        application.setStatus(newStatus);
        if (notes != null && !notes.isBlank()) {
            application.setRecruiterNotes(notes);
        }
        if (newStatus == ApplicationStatus.INTERVIEW_SCHEDULED && application.getInterviewDate() == null) {
            application.setInterviewDate(LocalDateTime.now().plusDays(3));
        }

        Application saved = applicationRepository.save(application);

        // Publish Asynchronous Domain Event
        if (currentStatus != newStatus) {
            eventPublisher.publishEvent(new com.recruitment.platform.event.ApplicationStatusChangedEvent(
                    saved.getId(),
                    saved.getCandidate().getId(),
                    saved.getCandidate().getEmail(),
                    saved.getCandidate().getFullName(),
                    saved.getJob().getId(),
                    saved.getJob().getTitle(),
                    currentStatus,
                    newStatus,
                    notes
            ));
        }

        return saved;
    }

    /**
     * Recruiter schedule interview with candidate, validating pipeline eligibility.
     */
    public Application scheduleInterview(Long applicationId, LocalDateTime interviewDateTime, String notes) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new com.recruitment.platform.exception.ResourceNotFoundException("Application not found: " + applicationId));

        ApplicationStatus currentStatus = application.getStatus();

        if (currentStatus.isTerminal()) {
            throw new com.recruitment.platform.exception.InvalidStateTransitionException(
                    "Cannot schedule interview from terminal state " + currentStatus + " for application id " + applicationId);
        }

        if (!currentStatus.canTransitionTo(ApplicationStatus.INTERVIEW_SCHEDULED)) {
            throw new com.recruitment.platform.exception.InvalidStateTransitionException(
                    currentStatus, ApplicationStatus.INTERVIEW_SCHEDULED);
        }

        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        application.setInterviewDate(interviewDateTime);
        if (notes != null) {
            application.setRecruiterNotes(notes);
        }

        Application saved = applicationRepository.save(application);

        if (currentStatus != ApplicationStatus.INTERVIEW_SCHEDULED) {
            eventPublisher.publishEvent(new com.recruitment.platform.event.ApplicationStatusChangedEvent(
                    saved.getId(),
                    saved.getCandidate().getId(),
                    saved.getCandidate().getEmail(),
                    saved.getCandidate().getFullName(),
                    saved.getJob().getId(),
                    saved.getJob().getTitle(),
                    currentStatus,
                    ApplicationStatus.INTERVIEW_SCHEDULED,
                    notes
            ));
        }

        return saved;
    }

    /**
     * Get aggregate statistics for the recruitment executive dashboard
     */
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalJobs(jobRepository.count());
        stats.setTotalCandidates(candidateRepository.count());
        stats.setTotalApplications(applicationRepository.count());
        stats.setShortlistedCount(applicationRepository.countByStatus(ApplicationStatus.SHORTLISTED));
        stats.setInterviewCount(applicationRepository.countByStatus(ApplicationStatus.INTERVIEW_SCHEDULED));

        Double avgScore = applicationRepository.getAverageMatchScore();
        stats.setAverageMatchScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 82.5);

        return stats;
    }
}
