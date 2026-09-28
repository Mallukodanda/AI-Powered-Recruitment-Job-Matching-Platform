package com.recruitment.platform.dto;

public class DashboardStatsDto {

    private long totalJobs;
    private long totalCandidates;
    private long totalApplications;
    private long shortlistedCount;
    private long interviewCount;
    private double averageMatchScore;

    public DashboardStatsDto() {}

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }

    public long getTotalCandidates() { return totalCandidates; }
    public void setTotalCandidates(long totalCandidates) { this.totalCandidates = totalCandidates; }

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public long getShortlistedCount() { return shortlistedCount; }
    public void setShortlistedCount(long shortlistedCount) { this.shortlistedCount = shortlistedCount; }

    public long getInterviewCount() { return interviewCount; }
    public void setInterviewCount(long interviewCount) { this.interviewCount = interviewCount; }

    public double getAverageMatchScore() { return averageMatchScore; }
    public void setAverageMatchScore(double averageMatchScore) { this.averageMatchScore = averageMatchScore; }
}
