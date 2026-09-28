package com.recruitment.platform.service;

import com.recruitment.platform.dto.SkillGapAnalysisDto;
import com.recruitment.platform.dto.StructuredJobData;
import com.recruitment.platform.exception.ResourceNotFoundException;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.repository.CandidateRepository;
import com.recruitment.platform.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class SkillGapService {

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final HybridJobMatchingService hybridJobMatchingService;

    public SkillGapService(JobRepository jobRepository,
                           CandidateRepository candidateRepository,
                           HybridJobMatchingService hybridJobMatchingService) {
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.hybridJobMatchingService = hybridJobMatchingService;
    }

    /**
     * Conducts deep skill-gap analysis comparing job requirements against candidate evidence.
     */
    public SkillGapAnalysisDto analyzeSkillGap(Long jobId, Long candidateId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        StructuredJobData jobData = hybridJobMatchingService.getStructuredJobData(job);

        // 1. Candidate Primary Evidence (Explicit verified skills)
        Set<String> explicitCandidateSkills = extractExplicitCandidateSkills(candidate);

        // 2. Candidate Secondary Context (Resume text, projects, bio, experience descriptions)
        String secondaryContext = buildSecondaryCandidateContext(candidate);

        List<String> presentSkills = new ArrayList<>();
        List<String> lessEvidentSkills = new ArrayList<>();
        List<String> missingRequiredSkills = new ArrayList<>();
        List<String> missingPreferredSkills = new ArrayList<>();
        List<String> criticalGaps = new ArrayList<>();
        List<String> learningPlan = new ArrayList<>();

        List<String> requiredSkills = jobData.getRequiredSkills() != null
                ? jobData.getRequiredSkills()
                : Collections.emptyList();

        List<String> preferredSkills = jobData.getPreferredSkills() != null
                ? jobData.getPreferredSkills()
                : Collections.emptyList();

        // Evaluate Required Skills
        int presentRequiredCount = 0;
        int lessEvidentRequiredCount = 0;

        for (String reqSkill : requiredSkills) {
            String cleanSkill = reqSkill.trim();
            if (isExplicitMatch(cleanSkill, explicitCandidateSkills)) {
                presentSkills.add(cleanSkill);
                presentRequiredCount++;
            } else if (isSecondaryContextMatch(cleanSkill, secondaryContext)) {
                lessEvidentSkills.add(cleanSkill + " (Referenced in experience/projects, needs deeper verification)");
                lessEvidentRequiredCount++;
                learningPlan.add("Document and quantify your hands-on achievements utilizing " + cleanSkill + " in your project portfolio.");
            } else {
                missingRequiredSkills.add(cleanSkill);
                criticalGaps.add(cleanSkill);
                learningPlan.add("Build a production-grade demonstration project focusing on " + cleanSkill + " and core architectural patterns.");
            }
        }

        // Evaluate Preferred Skills
        for (String prefSkill : preferredSkills) {
            String cleanSkill = prefSkill.trim();
            if (isExplicitMatch(cleanSkill, explicitCandidateSkills)) {
                if (!presentSkills.contains(cleanSkill)) {
                    presentSkills.add(cleanSkill + " (Bonus skill)");
                }
            } else if (isSecondaryContextMatch(cleanSkill, secondaryContext)) {
                if (!lessEvidentSkills.contains(cleanSkill)) {
                    lessEvidentSkills.add(cleanSkill + " (Bonus skill referenced in profile)");
                }
            } else {
                missingPreferredSkills.add(cleanSkill);
            }
        }

        // Compute Readiness Score
        int totalRequired = Math.max(1, requiredSkills.size());
        double readiness = ((presentRequiredCount * 1.0) + (lessEvidentRequiredCount * 0.5)) / totalRequired * 100.0;
        double roundedReadiness = Math.round(readiness * 10.0) / 10.0;

        String readinessLevel;
        if (roundedReadiness >= 80.0) {
            readinessLevel = "JOB_READY";
        } else if (roundedReadiness >= 50.0) {
            readinessLevel = "MODERATE_GAP";
        } else {
            readinessLevel = "SIGNIFICANT_GAP";
        }

        SkillGapAnalysisDto result = new SkillGapAnalysisDto();
        result.setJobId(job.getId());
        result.setJobTitle(job.getTitle());
        result.setCandidateId(candidate.getId());
        result.setCandidateName(candidate.getFullName());
        result.setPresentSkills(presentSkills);
        result.setLessEvidentSkills(lessEvidentSkills);
        result.setMissingRequiredSkills(missingRequiredSkills);
        result.setMissingPreferredSkills(missingPreferredSkills);
        result.setCriticalGaps(criticalGaps);
        result.setReadinessScore(roundedReadiness);
        result.setReadinessLevel(readinessLevel);
        result.setActionableLearningPlan(learningPlan);
        result.setEvaluatedAt(LocalDateTime.now());

        return result;
    }

    private Set<String> extractExplicitCandidateSkills(Candidate candidate) {
        Set<String> skills = new HashSet<>();
        if (candidate.getSkills() != null) {
            candidate.getSkills().forEach(s -> {
                if (s.getName() != null) skills.add(s.getName().trim().toLowerCase());
            });
        }
        if (candidate.getSkillsSummary() != null && !candidate.getSkillsSummary().isBlank()) {
            for (String part : candidate.getSkillsSummary().split("[,;|]")) {
                if (!part.isBlank()) skills.add(part.trim().toLowerCase());
            }
        }
        return skills;
    }

    private String buildSecondaryCandidateContext(Candidate candidate) {
        StringBuilder sb = new StringBuilder();
        if (candidate.getResumeText() != null) sb.append(candidate.getResumeText()).append(" ");
        if (candidate.getBio() != null) sb.append(candidate.getBio()).append(" ");
        if (candidate.getHeadline() != null) sb.append(candidate.getHeadline()).append(" ");
        if (candidate.getCurrentTitle() != null) sb.append(candidate.getCurrentTitle()).append(" ");

        if (candidate.getProjects() != null) {
            candidate.getProjects().forEach(p -> {
                if (p.getTitle() != null) sb.append(p.getTitle()).append(" ");
                if (p.getDescription() != null) sb.append(p.getDescription()).append(" ");
                if (p.getTechnologies() != null) sb.append(p.getTechnologies()).append(" ");
            });
        }

        if (candidate.getExperiences() != null) {
            candidate.getExperiences().forEach(e -> {
                if (e.getTitle() != null) sb.append(e.getTitle()).append(" ");
                if (e.getDescription() != null) sb.append(e.getDescription()).append(" ");
            });
        }

        return sb.toString().toLowerCase();
    }

    private boolean isExplicitMatch(String skill, Set<String> candidateSkills) {
        String lower = skill.toLowerCase();
        if (candidateSkills.contains(lower)) return true;
        for (String cs : candidateSkills) {
            if (cs.contains(lower) || lower.contains(cs)) return true;
        }
        return false;
    }

    private boolean isSecondaryContextMatch(String skill, String secondaryContext) {
        if (secondaryContext == null || secondaryContext.isBlank()) return false;
        String lower = skill.toLowerCase();
        return secondaryContext.contains(lower);
    }
}
