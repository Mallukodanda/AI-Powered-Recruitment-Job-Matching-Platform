package com.recruitment.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.platform.dto.ExplainableMatchResult;
import com.recruitment.platform.dto.MatchScoreResponse;
import com.recruitment.platform.dto.StructuredJobData;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.CandidateEmbedding;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.model.JobAnalysis;
import com.recruitment.platform.repository.CandidateEmbeddingRepository;
import com.recruitment.platform.repository.JobAnalysisRepository;
import com.recruitment.platform.service.ai.EmbeddingService;
import com.recruitment.platform.service.ai.JobDescriptionAnalyzer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class HybridJobMatchingService {

    // Signal weights (Sum = 1.00)
    public static final double WEIGHT_REQUIRED_SKILLS = 0.35;
    public static final double WEIGHT_SEMANTIC_SIMILARITY = 0.25;
    public static final double WEIGHT_EXPERIENCE = 0.15;
    public static final double WEIGHT_TECHNOLOGY = 0.10;
    public static final double WEIGHT_PREFERRED_SKILLS = 0.10;
    public static final double WEIGHT_EDUCATION = 0.05;

    private final JobAnalysisRepository jobAnalysisRepository;
    private final CandidateEmbeddingRepository candidateEmbeddingRepository;
    private final JobDescriptionAnalyzer jobDescriptionAnalyzer;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    public HybridJobMatchingService(JobAnalysisRepository jobAnalysisRepository,
                                    CandidateEmbeddingRepository candidateEmbeddingRepository,
                                    JobDescriptionAnalyzer jobDescriptionAnalyzer,
                                    EmbeddingService embeddingService,
                                    ObjectMapper objectMapper) {
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.candidateEmbeddingRepository = candidateEmbeddingRepository;
        this.jobDescriptionAnalyzer = jobDescriptionAnalyzer;
        this.embeddingService = embeddingService;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes the explainable hybrid matching algorithm between a Job and Candidate.
     */
    public ExplainableMatchResult match(Job job, Candidate candidate) {
        ExplainableMatchResult result = new ExplainableMatchResult();

        if (job == null || candidate == null) {
            result.setOverallScore(0.0);
            result.setCompatibilityBand("GROWTH_ALIGNMENT");
            result.setDecisionSupportSummary("Insufficient job or candidate profile context available for evaluation.");
            return result;
        }

        // 1. Obtain structured job data (cached or newly analyzed)
        StructuredJobData jobData = getStructuredJobData(job);

        // 2. Candidate extracted profile text and skills
        String candidateResume = candidate.getResumeText() != null ? candidate.getResumeText() : "";
        String candidateFullText = ((candidate.getCurrentTitle() != null ? candidate.getCurrentTitle() : "") + " " +
                (candidate.getHeadline() != null ? candidate.getHeadline() : "") + " " +
                (candidate.getSkillsSummary() != null ? candidate.getSkillsSummary() : "") + " " +
                (candidate.getBio() != null ? candidate.getBio() : "") + " " +
                candidateResume).toLowerCase();

        Set<String> candidateSkills = extractCandidateSkills(candidate);

        // ---------------------------------------------------------------------
        // SIGNAL 1: Required Skills Overlap (Weight: 35%)
        // ---------------------------------------------------------------------
        List<String> matchedRequired = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();

        List<String> reqSkills = jobData.getRequiredSkills();
        if (reqSkills == null || reqSkills.isEmpty()) {
            // Fallback to job's raw skills if required list is empty
            reqSkills = parseCommaSeparated(job.getSkills());
        }

        for (String rSkill : reqSkills) {
            String clean = rSkill.trim();
            if (clean.isBlank()) continue;
            boolean present = candidateSkills.stream().anyMatch(cs -> cs.equalsIgnoreCase(clean)) ||
                    containsWord(candidateFullText, clean.toLowerCase());
            if (present) {
                matchedRequired.add(clean);
            } else {
                missingRequired.add(clean);
            }
        }

        double requiredSkillScore = reqSkills.isEmpty() ? 85.0 :
                ((double) matchedRequired.size() / reqSkills.size()) * 100.0;
        requiredSkillScore = Math.max(0.0, Math.min(100.0, requiredSkillScore));
        result.setRequiredSkillScore(round(requiredSkillScore));
        result.setMatchedRequiredSkills(matchedRequired);
        result.setMissingRequiredSkills(missingRequired);

        // ---------------------------------------------------------------------
        // SIGNAL 2: Semantic Similarity via Dense Embeddings (Weight: 25%)
        // ---------------------------------------------------------------------
        double semanticScore;
        try {
            float[] jobEmbedding = getOrComputeJobEmbedding(job, jobData);
            float[] candidateEmbedding = getOrComputeCandidateEmbedding(candidate, candidateFullText);
            double cosine = embeddingService.computeCosineSimilarity(jobEmbedding, candidateEmbedding);
            semanticScore = Math.max(0.0, Math.min(100.0, cosine * 100.0));
        } catch (Exception e) {
            // Graceful fallback to lexical overlap on embedding service failure
            semanticScore = Math.max(30.0, requiredSkillScore * 0.8);
        }
        result.setSemanticScore(round(semanticScore));

        // ---------------------------------------------------------------------
        // SIGNAL 3: Preferred Skills Overlap (Weight: 10%)
        // ---------------------------------------------------------------------
        List<String> matchedPreferred = new ArrayList<>();
        List<String> prefSkills = jobData.getPreferredSkills();
        if (prefSkills != null && !prefSkills.isEmpty()) {
            for (String pSkill : prefSkills) {
                String clean = pSkill.trim();
                boolean present = candidateSkills.stream().anyMatch(cs -> cs.equalsIgnoreCase(clean)) ||
                        containsWord(candidateFullText, clean.toLowerCase());
                if (present) matchedPreferred.add(clean);
            }
        }
        double preferredSkillScore = (prefSkills == null || prefSkills.isEmpty()) ? 80.0 :
                ((double) matchedPreferred.size() / prefSkills.size()) * 100.0;
        preferredSkillScore = Math.max(0.0, Math.min(100.0, preferredSkillScore));
        result.setPreferredSkillScore(round(preferredSkillScore));
        result.setMatchedPreferredSkills(matchedPreferred);

        // ---------------------------------------------------------------------
        // SIGNAL 4: Experience Alignment (Weight: 15%)
        // ---------------------------------------------------------------------
        int requiredYears = jobData.getMinExperienceYears() != null ? jobData.getMinExperienceYears() :
                (job.getMinExperienceYears() != null ? job.getMinExperienceYears() : 2);
        int candidateYears = candidate.getYearsExperience() != null ? candidate.getYearsExperience() : 0;

        double experienceScore;
        if (candidateYears >= requiredYears) {
            experienceScore = 100.0;
            result.setExperienceAlignment(String.format("Candidate possesses %d years of experience, meeting/exceeding the required %d years.", candidateYears, requiredYears));
        } else {
            experienceScore = Math.max(20.0, ((double) candidateYears / requiredYears) * 100.0);
            result.setExperienceAlignment(String.format("Candidate possesses %d years of experience vs %d years required for this role.", candidateYears, requiredYears));
        }
        result.setCandidateExperienceYears(candidateYears);
        result.setRequiredExperienceYears(requiredYears);
        result.setExperienceScore(round(experienceScore));

        // ---------------------------------------------------------------------
        // SIGNAL 5: Education Alignment (Weight: 5%)
        // ---------------------------------------------------------------------
        double educationScore = evaluateEducation(candidate.getHighestEducation(), jobData.getEducationRequirement());
        result.setEducationScore(round(educationScore));
        result.setEducationAlignment(String.format("Candidate academic credential (%s) evaluated against requirement (%s).",
                candidate.getHighestEducation() != null ? candidate.getHighestEducation() : "Not specified",
                jobData.getEducationRequirement() != null ? jobData.getEducationRequirement() : "ANY"));

        // ---------------------------------------------------------------------
        // SIGNAL 6: Technology Relevance (Weight: 10%)
        // ---------------------------------------------------------------------
        List<String> relevantTech = new ArrayList<>();
        List<String> jobTech = jobData.getTechnologies();
        if (jobTech != null) {
            for (String tech : jobTech) {
                if (candidateSkills.stream().anyMatch(cs -> cs.equalsIgnoreCase(tech)) ||
                        containsWord(candidateFullText, tech.toLowerCase())) {
                    relevantTech.add(tech);
                }
            }
        }
        double techScore = (jobTech == null || jobTech.isEmpty()) ? 80.0 :
                ((double) relevantTech.size() / jobTech.size()) * 100.0;
        techScore = Math.max(0.0, Math.min(100.0, techScore));
        result.setTechnologyScore(round(techScore));
        result.setRelevantTechnologies(relevantTech);

        // ---------------------------------------------------------------------
        // HYBRID WEIGHTED AGGREGATION
        // ---------------------------------------------------------------------
        double overall = (requiredSkillScore * WEIGHT_REQUIRED_SKILLS) +
                (semanticScore * WEIGHT_SEMANTIC_SIMILARITY) +
                (experienceScore * WEIGHT_EXPERIENCE) +
                (techScore * WEIGHT_TECHNOLOGY) +
                (preferredSkillScore * WEIGHT_PREFERRED_SKILLS) +
                (educationScore * WEIGHT_EDUCATION);

        overall = round(Math.max(0.0, Math.min(100.0, overall)));
        result.setOverallScore(overall);

        // Determine Compatibility Band
        if (overall >= 85.0) {
            result.setCompatibilityBand("HIGH_ALIGNMENT");
        } else if (overall >= 70.0) {
            result.setCompatibilityBand("STRONG_ALIGNMENT");
        } else if (overall >= 50.0) {
            result.setCompatibilityBand("MODERATE_ALIGNMENT");
        } else {
            result.setCompatibilityBand("GROWTH_ALIGNMENT");
        }

        // Generate Evidence Notes & Assistive Summary
        generateEvidenceAndSummary(result, job, candidate);
        return result;
    }

    /**
     * Bridges explainable result to legacy MatchScoreResponse for backward compatibility.
     */
    public MatchScoreResponse calculateMatch(Job job, Candidate candidate) {
        ExplainableMatchResult r = match(job, candidate);
        MatchScoreResponse resp = new MatchScoreResponse();
        resp.setOverallScore(r.getOverallScore());
        resp.setSkillScore(r.getRequiredSkillScore());
        resp.setExperienceScore(r.getExperienceScore());
        resp.setEducationScore(r.getEducationScore());
        resp.setSemanticScore(r.getSemanticScore());
        resp.setMatchedSkills(r.getMatchedRequiredSkills());
        resp.setMissingSkills(r.getMissingRequiredSkills());
        resp.setBonusSkills(r.getMatchedPreferredSkills());

        // Map band to suitability level
        if ("HIGH_ALIGNMENT".equals(r.getCompatibilityBand())) {
            resp.setSuitabilityLevel("EXCELLENT");
        } else if ("STRONG_ALIGNMENT".equals(r.getCompatibilityBand())) {
            resp.setSuitabilityLevel("STRONG");
        } else if ("MODERATE_ALIGNMENT".equals(r.getCompatibilityBand())) {
            resp.setSuitabilityLevel("MODERATE");
        } else {
            resp.setSuitabilityLevel("LOW");
        }

        resp.setRecommendationRationale(r.getDecisionSupportSummary());
        resp.setRecommendedNextSteps(r.getRequirementAlignmentNotes());
        return resp;
    }

    // =========================================================================
    // EMBEDDING & CACHING PIPELINE
    // =========================================================================

    public StructuredJobData getStructuredJobData(Job job) {
        String jobSourceText = assembleJobText(job);
        String hash = embeddingService.computeSha256(jobSourceText);

        Optional<JobAnalysis> cached = jobAnalysisRepository.findByJobIdAndSha256Hash(job.getId(), hash);
        if (cached.isPresent() && cached.get().getStructuredDataJson() != null) {
            try {
                return objectMapper.readValue(cached.get().getStructuredDataJson(), StructuredJobData.class);
            } catch (Exception ignored) {}
        }

        // Analyze and cache
        StructuredJobData data;
        try {
            data = jobDescriptionAnalyzer.analyzeJob(
                    job.getTitle(),
                    job.getDescription(),
                    job.getRequirements(),
                    job.getSkills(),
                    job.getMinExperienceYears(),
                    null
            );
        } catch (Exception e) {
            // AI failure boundary: Fallback to structured defaults
            data = new StructuredJobData();
            data.setJobTitle(job.getTitle() != null ? job.getTitle() : "Software Role");
            data.setRequiredSkills(parseCommaSeparated(job.getSkills()));
            data.setTechnologies(parseCommaSeparated(job.getSkills()));
            data.setMinExperienceYears(job.getMinExperienceYears() != null ? job.getMinExperienceYears() : 2);
            data.setEducationRequirement("BACHELOR");
        }

        try {
            JobAnalysis analysis = jobAnalysisRepository.findByJobId(job.getId())
                    .orElse(new JobAnalysis(job, hash));
            analysis.setSha256Hash(hash);
            analysis.setStructuredDataJson(objectMapper.writeValueAsString(data));
            analysis.setAiProvider(jobDescriptionAnalyzer.getProviderName());
            analysis.setAiModel(jobDescriptionAnalyzer.getModelName());
            analysis.setStatus("ANALYZED");

            float[] vector = embeddingService.generateEmbedding(jobSourceText);
            analysis.setEmbeddingJson(embeddingService.serializeVector(vector));
            analysis.setUpdatedAt(LocalDateTime.now());
            jobAnalysisRepository.save(analysis);
        } catch (Exception ignored) {}

        return data;
    }

    public float[] getJobEmbedding(Job job) {
        return getOrComputeJobEmbedding(job, null);
    }

    public float[] getCandidateEmbedding(Candidate candidate) {
        String candidateResume = candidate.getResumeText() != null ? candidate.getResumeText() : "";
        String candidateFullText = ((candidate.getCurrentTitle() != null ? candidate.getCurrentTitle() : "") + " " +
                (candidate.getHeadline() != null ? candidate.getHeadline() : "") + " " +
                (candidate.getSkillsSummary() != null ? candidate.getSkillsSummary() : "") + " " +
                (candidate.getBio() != null ? candidate.getBio() : "") + " " +
                candidateResume).toLowerCase();
        return getOrComputeCandidateEmbedding(candidate, candidateFullText);
    }

    private float[] getOrComputeJobEmbedding(Job job, StructuredJobData jobData) {
        String jobSourceText = assembleJobText(job);
        String hash = embeddingService.computeSha256(jobSourceText);

        Optional<JobAnalysis> cached = jobAnalysisRepository.findByJobIdAndSha256Hash(job.getId(), hash);
        if (cached.isPresent() && cached.get().getEmbeddingJson() != null && !cached.get().getEmbeddingJson().isBlank()) {
            return embeddingService.deserializeVector(cached.get().getEmbeddingJson());
        }

        float[] vector = embeddingService.generateEmbedding(jobSourceText);
        try {
            JobAnalysis analysis = cached.orElseGet(() -> new JobAnalysis(job, hash));
            analysis.setSha256Hash(hash);
            analysis.setEmbeddingJson(embeddingService.serializeVector(vector));
            analysis.setUpdatedAt(LocalDateTime.now());
            jobAnalysisRepository.save(analysis);
        } catch (Exception ignored) {}

        return vector;
    }

    private float[] getOrComputeCandidateEmbedding(Candidate candidate, String candidateFullText) {
        String hash = embeddingService.computeSha256(candidateFullText);

        if (candidate.getId() != null) {
            Optional<CandidateEmbedding> cached = candidateEmbeddingRepository.findByCandidateIdAndSha256Hash(candidate.getId(), hash);
            if (cached.isPresent()) {
                return embeddingService.deserializeVector(cached.get().getEmbeddingJson());
            }
        }

        float[] vector = embeddingService.generateEmbedding(candidateFullText);
        if (candidate.getId() != null) {
            try {
                CandidateEmbedding ce = candidateEmbeddingRepository.findByCandidateId(candidate.getId())
                        .orElse(new CandidateEmbedding(candidate, hash, embeddingService.serializeVector(vector)));
                ce.setSha256Hash(hash);
                ce.setEmbeddingJson(embeddingService.serializeVector(vector));
                ce.setUpdatedAt(LocalDateTime.now());
                candidateEmbeddingRepository.save(ce);
            } catch (Exception ignored) {}
        }
        return vector;
    }

    // =========================================================================
    // HELPER FUNCTIONS
    // =========================================================================

    private String assembleJobText(Job job) {
        return (job.getTitle() != null ? job.getTitle() : "") + " " +
                (job.getDepartment() != null ? job.getDepartment() : "") + " " +
                (job.getDescription() != null ? job.getDescription() : "") + " " +
                (job.getRequirements() != null ? job.getRequirements() : "") + " " +
                (job.getSkills() != null ? job.getSkills() : "");
    }

    private Set<String> extractCandidateSkills(Candidate candidate) {
        Set<String> set = new LinkedHashSet<>();
        if (candidate.getSkills() != null) {
            for (var skill : candidate.getSkills()) {
                if (skill.getName() != null && !skill.getName().isBlank()) {
                    set.add(skill.getName().trim());
                }
            }
        }
        if (candidate.getSkillsSummary() != null && !candidate.getSkillsSummary().isBlank()) {
            for (String s : candidate.getSkillsSummary().split(",")) {
                if (!s.isBlank()) set.add(s.trim());
            }
        }
        return set;
    }

    private double evaluateEducation(String candidateEdu, String requiredEdu) {
        if (requiredEdu == null || "ANY".equalsIgnoreCase(requiredEdu)) {
            return 85.0;
        }
        if (candidateEdu == null || candidateEdu.isBlank()) {
            return 70.0;
        }

        String lowerC = candidateEdu.toLowerCase();
        String lowerR = requiredEdu.toLowerCase();

        int candidateLevel = getEducationLevelRank(lowerC);
        int requiredLevel = getEducationLevelRank(lowerR);

        if (candidateLevel >= requiredLevel) {
            return 100.0;
        } else if (candidateLevel == requiredLevel - 1) {
            return 80.0;
        } else {
            return 65.0;
        }
    }

    private int getEducationLevelRank(String edu) {
        if (edu.contains("ph.d") || edu.contains("doctorate")) return 4;
        if (edu.contains("master") || edu.contains("m.s") || edu.contains("msc")) return 3;
        if (edu.contains("bachelor") || edu.contains("b.s") || edu.contains("bsc") || edu.contains("degree")) return 2;
        return 1;
    }

    private void generateEvidenceAndSummary(ExplainableMatchResult result, Job job, Candidate candidate) {
        List<String> notes = new ArrayList<>();

        if (!result.getMatchedRequiredSkills().isEmpty()) {
            notes.add(String.format("Verified %d mandatory skill(s): %s",
                    result.getMatchedRequiredSkills().size(),
                    String.join(", ", result.getMatchedRequiredSkills().stream().limit(4).toList())));
        }
        if (!result.getMissingRequiredSkills().isEmpty()) {
            notes.add(String.format("Identified %d gap(s) in required skills: %s",
                    result.getMissingRequiredSkills().size(),
                    String.join(", ", result.getMissingRequiredSkills().stream().limit(4).toList())));
        }
        if (!result.getRelevantTechnologies().isEmpty()) {
            notes.add(String.format("Shared technology background: %s",
                    String.join(", ", result.getRelevantTechnologies().stream().limit(5).toList())));
        }
        notes.add(result.getExperienceAlignment());

        result.setRequirementAlignmentNotes(notes);

        // Responsible AI Summary: Assistive, explainable, and non-autonomous
        String summary = String.format(
                "Assistive evaluation for role '%s': Candidate exhibits %s (Score: %.1f%%). " +
                "Profile reflects %d of %d required skills with %d years of documented background. " +
                "Recommendation: Use this profile breakdown as decision support for human recruiter review.",
                job.getTitle(), result.getCompatibilityBand(), result.getOverallScore(),
                result.getMatchedRequiredSkills().size(),
                (result.getMatchedRequiredSkills().size() + result.getMissingRequiredSkills().size()),
                result.getCandidateExperienceYears());

        result.setDecisionSupportSummary(summary);
    }

    private boolean containsWord(String fullText, String word) {
        if (fullText == null || word == null || word.isBlank()) return false;
        return fullText.contains(word);
    }

    private List<String> parseCommaSeparated(String input) {
        if (input == null || input.isBlank()) return new ArrayList<>();
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private double round(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
