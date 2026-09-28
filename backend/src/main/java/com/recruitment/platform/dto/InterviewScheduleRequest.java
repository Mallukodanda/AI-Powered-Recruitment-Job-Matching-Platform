package com.recruitment.platform.dto;

import com.recruitment.platform.model.InterviewType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;

public class InterviewScheduleRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    private Long interviewerId;

    private Long applicationId;

    @NotBlank(message = "Interview title is required")
    private String title = "Technical Interview";

    private InterviewType interviewType = InterviewType.TECHNICAL;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Interview must be scheduled for a future date and time")
    private ZonedDateTime scheduledTime;

    private String timeZone = "UTC";

    private Integer durationMinutes = 60;

    private String meetingLink;

    private String notes;

    public InterviewScheduleRequest() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public Long getInterviewerId() { return interviewerId; }
    public void setInterviewerId(Long interviewerId) { this.interviewerId = interviewerId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public InterviewType getInterviewType() { return interviewType; }
    public void setInterviewType(InterviewType interviewType) { this.interviewType = interviewType; }

    public ZonedDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(ZonedDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
