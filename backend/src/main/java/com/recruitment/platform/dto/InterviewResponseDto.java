package com.recruitment.platform.dto;

import com.recruitment.platform.model.Interview;
import com.recruitment.platform.model.InterviewStatus;
import com.recruitment.platform.model.InterviewType;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class InterviewResponseDto {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long interviewerId;
    private String interviewerName;
    private String interviewerEmail;
    private Long applicationId;
    private String title;
    private InterviewType interviewType;
    private InterviewStatus status;
    private ZonedDateTime scheduledTimeUtc;
    private String scheduledTimeFormattedUtc;
    private String scheduledTimeFormattedLocal;
    private String timeZone;
    private Integer durationMinutes;
    private String meetingLink;
    private String recruiterNotes;
    private String interviewerFeedback;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InterviewResponseDto fromEntity(Interview interview) {
        if (interview == null) {
            return null;
        }

        InterviewResponseDto dto = new InterviewResponseDto();
        dto.setId(interview.getId());
        if (interview.getJob() != null) {
            dto.setJobId(interview.getJob().getId());
            dto.setJobTitle(interview.getJob().getTitle());
        }
        if (interview.getCandidate() != null) {
            dto.setCandidateId(interview.getCandidate().getId());
            dto.setCandidateName(interview.getCandidate().getFullName());
            dto.setCandidateEmail(interview.getCandidate().getEmail());
        }
        if (interview.getInterviewer() != null) {
            dto.setInterviewerId(interview.getInterviewer().getId());
            dto.setInterviewerName(interview.getInterviewer().getFullName());
            dto.setInterviewerEmail(interview.getInterviewer().getEmail());
        }
        if (interview.getApplication() != null) {
            dto.setApplicationId(interview.getApplication().getId());
        }
        dto.setTitle(interview.getTitle());
        dto.setInterviewType(interview.getInterviewType());
        dto.setStatus(interview.getStatus());
        dto.setScheduledTimeUtc(interview.getScheduledTime());
        dto.setTimeZone(interview.getTimeZone());
        dto.setDurationMinutes(interview.getDurationMinutes());
        dto.setMeetingLink(interview.getMeetingLink());
        dto.setRecruiterNotes(interview.getRecruiterNotes());
        dto.setInterviewerFeedback(interview.getInterviewerFeedback());
        dto.setRating(interview.getRating());
        dto.setCreatedAt(interview.getCreatedAt());
        dto.setUpdatedAt(interview.getUpdatedAt());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z (VV)");
        if (interview.getScheduledTime() != null) {
            dto.setScheduledTimeFormattedUtc(interview.getScheduledTime().format(formatter));
            ZonedDateTime localTime = interview.getLocalScheduledTime();
            if (localTime != null) {
                dto.setScheduledTimeFormattedLocal(localTime.format(formatter));
            }
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public Long getInterviewerId() { return interviewerId; }
    public void setInterviewerId(Long interviewerId) { this.interviewerId = interviewerId; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public String getInterviewerEmail() { return interviewerEmail; }
    public void setInterviewerEmail(String interviewerEmail) { this.interviewerEmail = interviewerEmail; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public InterviewType getInterviewType() { return interviewType; }
    public void setInterviewType(InterviewType interviewType) { this.interviewType = interviewType; }

    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) { this.status = status; }

    public ZonedDateTime getScheduledTimeUtc() { return scheduledTimeUtc; }
    public void setScheduledTimeUtc(ZonedDateTime scheduledTimeUtc) { this.scheduledTimeUtc = scheduledTimeUtc; }

    public String getScheduledTimeFormattedUtc() { return scheduledTimeFormattedUtc; }
    public void setScheduledTimeFormattedUtc(String scheduledTimeFormattedUtc) { this.scheduledTimeFormattedUtc = scheduledTimeFormattedUtc; }

    public String getScheduledTimeFormattedLocal() { return scheduledTimeFormattedLocal; }
    public void setScheduledTimeFormattedLocal(String scheduledTimeFormattedLocal) { this.scheduledTimeFormattedLocal = scheduledTimeFormattedLocal; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

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
}
