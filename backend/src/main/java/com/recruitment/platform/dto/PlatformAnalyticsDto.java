package com.recruitment.platform.dto;

import java.util.HashMap;
import java.util.Map;

public class PlatformAnalyticsDto {

    private long totalUsers;
    private long totalCandidates;
    private long totalRecruiters;
    private long totalAdmins;
    private long totalCompanies;
    private long totalJobs;
    private long activeJobs;
    private long totalApplications;
    private long totalInterviews;

    private Map<String, Long> applicationsByStatus = new HashMap<>();
    private Map<String, Long> interviewsByStatus = new HashMap<>();
    private Map<String, Long> usersByRole = new HashMap<>();

    // AI & Intelligence Metrics
    private double averageMatchScore;
    private long totalScreened;
    private long autoShortlistedCount;
    private long totalResumesParsed;
    private long totalEmbeddings;

    public PlatformAnalyticsDto() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalCandidates() { return totalCandidates; }
    public void setTotalCandidates(long totalCandidates) { this.totalCandidates = totalCandidates; }

    public long getTotalRecruiters() { return totalRecruiters; }
    public void setTotalRecruiters(long totalRecruiters) { this.totalRecruiters = totalRecruiters; }

    public long getTotalAdmins() { return totalAdmins; }
    public void setTotalAdmins(long totalAdmins) { this.totalAdmins = totalAdmins; }

    public long getTotalCompanies() { return totalCompanies; }
    public void setTotalCompanies(long totalCompanies) { this.totalCompanies = totalCompanies; }

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }

    public long getActiveJobs() { return activeJobs; }
    public void setActiveJobs(long activeJobs) { this.activeJobs = activeJobs; }

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public long getTotalInterviews() { return totalInterviews; }
    public void setTotalInterviews(long totalInterviews) { this.totalInterviews = totalInterviews; }

    public Map<String, Long> getApplicationsByStatus() { return applicationsByStatus; }
    public void setApplicationsByStatus(Map<String, Long> applicationsByStatus) { this.applicationsByStatus = applicationsByStatus; }

    public Map<String, Long> getInterviewsByStatus() { return interviewsByStatus; }
    public void setInterviewsByStatus(Map<String, Long> interviewsByStatus) { this.interviewsByStatus = interviewsByStatus; }

    public Map<String, Long> getUsersByRole() { return usersByRole; }
    public void setUsersByRole(Map<String, Long> usersByRole) { this.usersByRole = usersByRole; }

    public double getAverageMatchScore() { return averageMatchScore; }
    public void setAverageMatchScore(double averageMatchScore) { this.averageMatchScore = averageMatchScore; }

    public long getTotalScreened() { return totalScreened; }
    public void setTotalScreened(long totalScreened) { this.totalScreened = totalScreened; }

    public long getAutoShortlistedCount() { return autoShortlistedCount; }
    public void setAutoShortlistedCount(long autoShortlistedCount) { this.autoShortlistedCount = autoShortlistedCount; }

    public long getTotalResumesParsed() { return totalResumesParsed; }
    public void setTotalResumesParsed(long totalResumesParsed) { this.totalResumesParsed = totalResumesParsed; }

    public long getTotalEmbeddings() { return totalEmbeddings; }
    public void setTotalEmbeddings(long totalEmbeddings) { this.totalEmbeddings = totalEmbeddings; }
}
