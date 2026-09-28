package com.recruitment.platform.service;

import com.recruitment.platform.dto.CandidateRecommendationDto;
import com.recruitment.platform.dto.CandidateSummaryDto;
import com.recruitment.platform.dto.ExplainableMatchResult;
import com.recruitment.platform.dto.JobRecommendationDto;
import com.recruitment.platform.exception.ResourceNotFoundException;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.model.Role;
import com.recruitment.platform.model.User;
import com.recruitment.platform.repository.CandidateRepository;
import com.recruitment.platform.repository.JobRepository;
import com.recruitment.platform.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class RecommendationService {

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final HybridJobMatchingService hybridJobMatchingService;

    public RecommendationService(JobRepository jobRepository,
                                 CandidateRepository candidateRepository,
                                 UserRepository userRepository,
                                 HybridJobMatchingService hybridJobMatchingService) {
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.hybridJobMatchingService = hybridJobMatchingService;
    }

    /**
     * Recommends active jobs to a candidate based on multi-signal matching and preference alignment.
     */
    public List<JobRecommendationDto> getJobRecommendations(Long candidateId, Integer limit, String callerEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        verifyCandidateOrRecruiterAccess(candidate, callerEmail);

        List<Job> activeJobs = jobRepository.findByStatus("ACTIVE");
        if (activeJobs.isEmpty()) {
            return Collections.emptyList();
        }

        int maxResults = (limit != null && limit > 0) ? Math.min(limit, 50) : 10;
        List<JobRecommendationDto> recommendations = new ArrayList<>();

        for (Job job : activeJobs) {
            ExplainableMatchResult match = hybridJobMatchingService.match(job, candidate);
            double baseScore = match.getOverallScore();

            // Preference signal 1: Location & Remote Alignment
            boolean isRemote = job.getLocation() != null && job.getLocation().equalsIgnoreCase("Remote");
            boolean locationMatch = false;
            if (candidate.getLocation() != null && job.getLocation() != null) {
                locationMatch = candidate.getLocation().toLowerCase().contains(job.getLocation().toLowerCase())
                        || job.getLocation().toLowerCase().contains(candidate.getLocation().toLowerCase());
            }

            double preferenceBonus = 0.0;
            List<String> factors = new ArrayList<>();

            if (isRemote) {
                preferenceBonus += 3.0;
                factors.add("Work Arrangement: Remote opportunity provides high geographic flexibility");
            } else if (locationMatch) {
                preferenceBonus += 3.0;
                factors.add("Location Alignment: Local match with candidate location (" + candidate.getLocation() + ")");
            }

            // Experience check factor
            if (candidate.getYearsExperience() != null && job.getMinExperienceYears() != null) {
                if (candidate.getYearsExperience() >= job.getMinExperienceYears()) {
                    factors.add("Experience Match: " + candidate.getYearsExperience() + " years meets or exceeds " + job.getMinExperienceYears() + " year requirement");
                }
            }

            // Core skills matched factor
            if (!match.getMatchedRequiredSkills().isEmpty()) {
                factors.add("Key Skills Evidenced: " + String.join(", ", match.getMatchedRequiredSkills()));
            }

            // Semantic relevance factor
            if (match.getSemanticScore() >= 60.0) {
                factors.add("Semantic Context: High conceptual alignment with role responsibilities and domain");
            }

            double finalScore = Math.min(100.0, Math.round((baseScore + preferenceBonus) * 10.0) / 10.0);

            // Filter out negligible or zero-overlap jobs
            if (finalScore >= 20.0) {
                JobRecommendationDto dto = new JobRecommendationDto();
                dto.setJob(job);
                dto.setMatchScore(finalScore);
                dto.setMatchLevel(determineRecommendationLevel(finalScore));
                dto.setMatchingFactors(factors);
                dto.setMatchedSkills(match.getMatchedRequiredSkills());
                dto.setGrowthSkills(match.getMissingRequiredSkills());
                recommendations.add(dto);
            }
        }

        // Sort descending by recommendation match score
        recommendations.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        return recommendations.stream().limit(maxResults).collect(Collectors.toList());
    }

    /**
     * Recommends qualified candidates for an open job requisition (Authorized for Recruiters & Admins).
     */
    public List<CandidateRecommendationDto> getCandidateRecommendations(Long jobId, Integer limit, String callerEmail) {
        verifyRecruiterOrAdminAccess(callerEmail);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        List<Candidate> candidates = candidateRepository.findAll();
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        int maxResults = (limit != null && limit > 0) ? Math.min(limit, 50) : 10;
        List<CandidateRecommendationDto> recommendations = new ArrayList<>();

        for (Candidate c : candidates) {
            ExplainableMatchResult match = hybridJobMatchingService.match(job, c);
            double score = match.getOverallScore();

            if (score >= 25.0) {
                CandidateRecommendationDto dto = new CandidateRecommendationDto();
                dto.setCandidate(CandidateSummaryDto.fromEntity(c));
                dto.setMatchScore(score);
                dto.setMatchLevel(match.getCompatibilityBand() != null ? match.getCompatibilityBand() : "POTENTIAL_MATCH");
                dto.setMatchedRequiredSkills(match.getMatchedRequiredSkills());
                dto.setMissingRequiredSkills(match.getMissingRequiredSkills());
                dto.setYearsExperience(c.getYearsExperience() != null ? c.getYearsExperience() : 0);
                dto.setRationale(match.getDecisionSupportSummary());
                recommendations.add(dto);
            }
        }

        recommendations.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        return recommendations.stream().limit(maxResults).collect(Collectors.toList());
    }

    private String determineRecommendationLevel(double score) {
        if (score >= 80.0) return "STRONG_MATCH";
        if (score >= 65.0) return "GOOD_MATCH";
        if (score >= 50.0) return "MODERATE_MATCH";
        return "EXPLORATORY";
    }

    private void verifyCandidateOrRecruiterAccess(Candidate candidate, String callerEmail) {
        if (callerEmail == null || callerEmail.isBlank()) {
            return; // Allow public or local test execution if unauthenticated
        }
        Optional<User> userOpt = userRepository.findByEmail(callerEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() == Role.RECRUITER || user.getRole() == Role.ADMIN) {
                return;
            }
            if (user.getRole() == Role.CANDIDATE && callerEmail.equalsIgnoreCase(candidate.getEmail())) {
                return;
            }
            throw new AccessDeniedException("Access denied: You can only view recommendations for your own profile.");
        }
    }

    private void verifyRecruiterOrAdminAccess(String callerEmail) {
        if (callerEmail == null || callerEmail.isBlank()) {
            throw new AccessDeniedException("Authentication required to access candidate recommendations.");
        }
        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + callerEmail));

        if (user.getRole() != Role.RECRUITER && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: Candidate recommendations require RECRUITER or ADMIN authority.");
        }
    }
}
