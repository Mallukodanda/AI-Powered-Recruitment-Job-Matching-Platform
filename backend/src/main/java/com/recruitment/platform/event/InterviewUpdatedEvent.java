package com.recruitment.platform.event;

import com.recruitment.platform.model.InterviewStatus;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

public class InterviewUpdatedEvent {

    private final String eventId;
    private final Long interviewId;
    private final String candidateEmail;
    private final String interviewerEmail;
    private final String jobTitle;
    private final InterviewStatus status;
    private final ZonedDateTime scheduledTime;
    private final String timeZone;
    private final String notes;
    private final Instant timestamp;

    public InterviewUpdatedEvent(Long interviewId, String candidateEmail, String interviewerEmail,
                                 String jobTitle, InterviewStatus status, ZonedDateTime scheduledTime,
                                 String timeZone, String notes) {
        this.eventId = UUID.randomUUID().toString();
        this.interviewId = interviewId;
        this.candidateEmail = candidateEmail;
        this.interviewerEmail = interviewerEmail;
        this.jobTitle = jobTitle;
        this.status = status;
        this.scheduledTime = scheduledTime;
        this.timeZone = timeZone;
        this.notes = notes;
        this.timestamp = Instant.now();
    }

    public InterviewUpdatedEvent(String eventId, Long interviewId, String candidateEmail,
                                 String interviewerEmail, String jobTitle, InterviewStatus status,
                                 ZonedDateTime scheduledTime, String timeZone, String notes,
                                 Instant timestamp) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
        this.interviewId = interviewId;
        this.candidateEmail = candidateEmail;
        this.interviewerEmail = interviewerEmail;
        this.jobTitle = jobTitle;
        this.status = status;
        this.scheduledTime = scheduledTime;
        this.timeZone = timeZone;
        this.notes = notes;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getEventId() { return eventId; }
    public Long getInterviewId() { return interviewId; }
    public String getCandidateEmail() { return candidateEmail; }
    public String getInterviewerEmail() { return interviewerEmail; }
    public String getJobTitle() { return jobTitle; }
    public InterviewStatus getStatus() { return status; }
    public ZonedDateTime getScheduledTime() { return scheduledTime; }
    public String getTimeZone() { return timeZone; }
    public String getNotes() { return notes; }
    public Instant getTimestamp() { return timestamp; }
}
