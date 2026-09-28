package com.recruitment.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "ai_match_score")
    private Double aiMatchScore = 0.0;

    @Lob
    @Column(name = "ai_rationale", columnDefinition = "TEXT")
    private String aiRationale;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String recruiterNotes;

    @Column(name = "interview_date")
    private LocalDateTime interviewDate;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Application() {}

    public Application(Job job, Candidate candidate, ApplicationStatus status, Double aiMatchScore, String aiRationale) {
        this.job = job;
        this.candidate = candidate;
        this.status = status;
        this.aiMatchScore = aiMatchScore;
        this.aiRationale = aiRationale;
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { 
        this.status = status; 
        this.updatedAt = LocalDateTime.now();
    }

    public Double getAiMatchScore() { return aiMatchScore; }
    public void setAiMatchScore(Double aiMatchScore) { this.aiMatchScore = aiMatchScore; }

    public String getAiRationale() { return aiRationale; }
    public void setAiRationale(String aiRationale) { this.aiRationale = aiRationale; }

    public String getRecruiterNotes() { return recruiterNotes; }
    public void setRecruiterNotes(String recruiterNotes) { this.recruiterNotes = recruiterNotes; }

    public LocalDateTime getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDateTime interviewDate) { this.interviewDate = interviewDate; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
