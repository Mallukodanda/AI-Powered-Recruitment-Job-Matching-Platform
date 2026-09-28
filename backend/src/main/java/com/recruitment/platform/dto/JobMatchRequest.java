package com.recruitment.platform.dto;

public class JobMatchRequest {

    private Long jobId;
    private Long candidateId;
    private String customResumeText;

    public JobMatchRequest() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getCustomResumeText() { return customResumeText; }
    public void setCustomResumeText(String customResumeText) { this.customResumeText = customResumeText; }
}
