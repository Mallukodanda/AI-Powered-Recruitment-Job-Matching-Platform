package com.recruitment.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private User interviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false)
    private InterviewType interviewType = InterviewType.TECHNICAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    /**
     * Exact scheduled instant stored in UTC.
     */
    @Column(name = "scheduled_time", nullable = false)
    private ZonedDateTime scheduledTime;

    /**
     * The original IANA timezone identifier for the interview location / participants
     * (e.g. "America/New_York", "Asia/Kolkata", "Europe/London", "UTC").
     */
    @Column(name = "time_zone", nullable = false, length = 64)
    private String timeZone = "UTC";

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 60;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Lob
    @Column(name = "recruiter_notes", columnDefinition = "TEXT")
    private String recruiterNotes;

    @Lob
    @Column(name = "interviewer_feedback", columnDefinition = "TEXT")
    private String interviewerFeedback;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Interview() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public User getInterviewer() { return interviewer; }
    public void setInterviewer(User interviewer) { this.interviewer = interviewer; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public InterviewType getInterviewType() { return interviewType; }
    public void setInterviewType(InterviewType interviewType) { this.interviewType = interviewType; }

    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public ZonedDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(ZonedDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getRecruiterNotes() { return recruiterNotes; }
    public void setRecruiterNotes(String recruiterNotes) { this.recruiterNotes = recruiterNotes; }

    public String getInterviewerFeedback() { return interviewerFeedback; }
    public void setInterviewerFeedback(String interviewerFeedback) { this.interviewerFeedback = interviewerFeedback; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Computes the scheduled time converted to the interview's designated local timezone.
     */
    public ZonedDateTime getLocalScheduledTime() {
        if (scheduledTime == null) {
            return null;
        }
        ZoneId zone = (timeZone != null && !timeZone.isBlank()) ? ZoneId.of(timeZone) : ZoneId.of("UTC");
        return scheduledTime.withZoneSameInstant(zone);
    }
}
