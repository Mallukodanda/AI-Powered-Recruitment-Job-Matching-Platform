package com.recruitment.platform.service;

import com.recruitment.platform.dto.PlatformAnalyticsDto;
import com.recruitment.platform.model.ApplicationStatus;
import com.recruitment.platform.model.Role;
import com.recruitment.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class PlatformAnalyticsService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final CandidateEmbeddingRepository candidateEmbeddingRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    public PlatformAnalyticsService(UserRepository userRepository,
                                    CompanyRepository companyRepository,
                                    JobRepository jobRepository,
                                    ApplicationRepository applicationRepository,
                                    InterviewRepository interviewRepository,
                                    CandidateEmbeddingRepository candidateEmbeddingRepository,
                                    ResumeAnalysisRepository resumeAnalysisRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.candidateEmbeddingRepository = candidateEmbeddingRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    /**
     * Gathers platform-wide metrics using optimized SQL/JPQL aggregation queries.
     * Avoids loading raw entity rows into application memory.
     */
    @Transactional(readOnly = true)
    public PlatformAnalyticsDto getPlatformAnalytics() {
        PlatformAnalyticsDto dto = new PlatformAnalyticsDto();

        // 1. High-Level Entity Counts
        dto.setTotalUsers(userRepository.count());
        dto.setTotalCandidates(userRepository.countByRole(Role.CANDIDATE));
        dto.setTotalRecruiters(userRepository.countByRole(Role.RECRUITER));
        dto.setTotalAdmins(userRepository.countByRole(Role.ADMIN));
        dto.setTotalCompanies(companyRepository.count());
        dto.setTotalJobs(jobRepository.count());
        dto.setActiveJobs(jobRepository.findAll().stream().filter(j -> "ACTIVE".equalsIgnoreCase(j.getStatus())).count());
        dto.setTotalApplications(applicationRepository.count());
        dto.setTotalInterviews(interviewRepository.count());

        // 2. Application Status Distribution (GROUP BY)
        Map<String, Long> appStatusMap = new HashMap<>();
        List<Object[]> appGroups = applicationRepository.countApplicationsGroupedByStatus();
        if (appGroups != null) {
            for (Object[] row : appGroups) {
                if (row.length >= 2 && row[0] != null && row[1] != null) {
                    appStatusMap.put(row[0].toString(), ((Number) row[1]).longValue());
                }
            }
        }
        dto.setApplicationsByStatus(appStatusMap);

        // 3. Interview Status Distribution (GROUP BY)
        Map<String, Long> intStatusMap = new HashMap<>();
        List<Object[]> intGroups = interviewRepository.countInterviewsGroupedByStatus();
        if (intGroups != null) {
            for (Object[] row : intGroups) {
                if (row.length >= 2 && row[0] != null && row[1] != null) {
                    intStatusMap.put(row[0].toString(), ((Number) row[1]).longValue());
                }
            }
        }
        dto.setInterviewsByStatus(intStatusMap);

        // 4. User Role Distribution (GROUP BY)
        Map<String, Long> userRoleMap = new HashMap<>();
        List<Object[]> userGroups = userRepository.countUsersGroupedByRole();
        if (userGroups != null) {
            for (Object[] row : userGroups) {
                if (row.length >= 2 && row[0] != null && row[1] != null) {
                    userRoleMap.put(row[0].toString(), ((Number) row[1]).longValue());
                }
            }
        }
        dto.setUsersByRole(userRoleMap);

        // 5. AI Intelligence Metrics
        Double avgScore = applicationRepository.getAverageMatchScore();
        dto.setAverageMatchScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0);
        dto.setTotalScreened(applicationRepository.countByStatus(ApplicationStatus.AI_SCREENED) +
                             applicationRepository.countByStatus(ApplicationStatus.SHORTLISTED));
        dto.setAutoShortlistedCount(applicationRepository.countByStatus(ApplicationStatus.SHORTLISTED));
        dto.setTotalResumesParsed(resumeAnalysisRepository.count());
        dto.setTotalEmbeddings(candidateEmbeddingRepository.count());

        return dto;
    }
}
