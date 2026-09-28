package com.recruitment.platform.dto;

import com.recruitment.platform.model.InterviewStatus;
import java.time.ZonedDateTime;

public class InterviewUpdateRequest {

    private InterviewStatus status;
    private ZonedDateTime scheduledTime;
    private String timeZone;
    private Integer durationMinutes;
    private String meetingLink;
    private String notes;
    private String interviewerFeedback;
    private Integer rating;

    public InterviewUpdateRequest() {}

    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) { this.status = status; }

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

    public String getInterviewerFeedback() { return interviewerFeedback; }
    public void setInterviewerFeedback(String interviewerFeedback) { this.interviewerFeedback = interviewerFeedback; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
