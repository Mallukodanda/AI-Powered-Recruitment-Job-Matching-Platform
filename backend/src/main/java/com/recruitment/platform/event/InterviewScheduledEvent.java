package com.recruitment.platform.event;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

public class InterviewScheduledEvent {

    private final String eventId;
    private final Long interviewId;
    private final Long candidateId;
    private final String candidateEmail;
    private final String candidateName;
    private final Long jobId;
    private final String jobTitle;
    private final Long interviewerId;
    private final String interviewerEmail;
    private final String interviewerName;
    private final String interviewTitle;
    private final ZonedDateTime scheduledTime;
    private final String timeZone;
    private final String meetingLink;
    private final Instant timestamp;

    public InterviewScheduledEvent(Long interviewId, Long candidateId, String candidateEmail,
                                   String candidateName, Long jobId, String jobTitle,
                                   Long interviewerId, String interviewerEmail, String interviewerName,
                                   String interviewTitle, ZonedDateTime scheduledTime,
                                   String timeZone, String meetingLink) {
        this.eventId = UUID.randomUUID().toString();
        this.interviewId = interviewId;
        this.candidateId = candidateId;
        this.candidateEmail = candidateEmail;
        this.candidateName = candidateName;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.interviewerId = interviewerId;
        this.interviewerEmail = interviewerEmail;
        this.interviewerName = interviewerName;
        this.interviewTitle = interviewTitle;
        this.scheduledTime = scheduledTime;
        this.timeZone = timeZone;
        this.meetingLink = meetingLink;
        this.timestamp = Instant.now();
    }

    public InterviewScheduledEvent(String eventId, Long interviewId, Long candidateId,
                                   String candidateEmail, String candidateName, Long jobId,
                                   String jobTitle, Long interviewerId, String interviewerEmail,
                                   String interviewerName, String interviewTitle,
                                   ZonedDateTime scheduledTime, String timeZone,
                                   String meetingLink, Instant timestamp) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
        this.interviewId = interviewId;
        this.candidateId = candidateId;
        this.candidateEmail = candidateEmail;
        this.candidateName = candidateName;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.interviewerId = interviewerId;
        this.interviewerEmail = interviewerEmail;
        this.interviewerName = interviewerName;
        this.interviewTitle = interviewTitle;
        this.scheduledTime = scheduledTime;
        this.timeZone = timeZone;
        this.meetingLink = meetingLink;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getEventId() { return eventId; }
    public Long getInterviewId() { return interviewId; }
    public Long getCandidateId() { return candidateId; }
    public String getCandidateEmail() { return candidateEmail; }
    public String getCandidateName() { return candidateName; }
    public Long getJobId() { return jobId; }
    public String getJobTitle() { return jobTitle; }
    public Long getInterviewerId() { return interviewerId; }
    public String getInterviewerEmail() { return interviewerEmail; }
    public String getInterviewerName() { return interviewerName; }
    public String getInterviewTitle() { return interviewTitle; }
    public ZonedDateTime getScheduledTime() { return scheduledTime; }
    public String getTimeZone() { return timeZone; }
    public String getMeetingLink() { return meetingLink; }
    public Instant getTimestamp() { return timestamp; }
}
