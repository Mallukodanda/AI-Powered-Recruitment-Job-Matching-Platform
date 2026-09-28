package com.recruitment.platform.event;

import com.recruitment.platform.model.ApplicationStatus;

import java.time.Instant;
import java.util.UUID;

public class ApplicationStatusChangedEvent {

    private final String eventId;
    private final Long applicationId;
    private final Long candidateId;
    private final String candidateEmail;
    private final String candidateName;
    private final Long jobId;
    private final String jobTitle;
    private final ApplicationStatus oldStatus;
    private final ApplicationStatus newStatus;
    private final String notes;
    private final Instant timestamp;

    public ApplicationStatusChangedEvent(Long applicationId, Long candidateId, String candidateEmail,
                                         String candidateName, Long jobId, String jobTitle,
                                         ApplicationStatus oldStatus, ApplicationStatus newStatus,
                                         String notes) {
        this.eventId = UUID.randomUUID().toString();
        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.candidateEmail = candidateEmail;
        this.candidateName = candidateName;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.notes = notes;
        this.timestamp = Instant.now();
    }

    public ApplicationStatusChangedEvent(String eventId, Long applicationId, Long candidateId,
                                         String candidateEmail, String candidateName, Long jobId,
                                         String jobTitle, ApplicationStatus oldStatus,
                                         ApplicationStatus newStatus, String notes, Instant timestamp) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.candidateEmail = candidateEmail;
        this.candidateName = candidateName;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.notes = notes;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getEventId() { return eventId; }
    public Long getApplicationId() { return applicationId; }
    public Long getCandidateId() { return candidateId; }
    public String getCandidateEmail() { return candidateEmail; }
    public String getCandidateName() { return candidateName; }
    public Long getJobId() { return jobId; }
    public String getJobTitle() { return jobTitle; }
    public ApplicationStatus getOldStatus() { return oldStatus; }
    public ApplicationStatus getNewStatus() { return newStatus; }
    public String getNotes() { return notes; }
    public Instant getTimestamp() { return timestamp; }
}
