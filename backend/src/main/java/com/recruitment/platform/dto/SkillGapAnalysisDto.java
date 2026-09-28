package com.recruitment.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Deep skill-gap comparison between job requirements and candidate evidence")
public class SkillGapAnalysisDto {

    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;

    @Schema(description = "Skills strongly evidenced in candidate profile and explicitly required by the job")
    private List<String> presentSkills;

    @Schema(description = "Skills tangentially mentioned or present in projects/certifications but needing deeper verification")
    private List<String> lessEvidentSkills;

    @Schema(description = "Mandatory/required skills completely absent from candidate profile")
    private List<String> missingRequiredSkills;

    @Schema(description = "Preferred/bonus skills absent from candidate profile")
    private List<String> missingPreferredSkills;

    @Schema(description = "High-priority blocking skill gaps that most impact candidate qualification")
    private List<String> criticalGaps;

    @Schema(description = "Composite skill readiness percentage (0.0 to 100.0)")
    private Double readinessScore;

    @Schema(description = "Categorization: JOB_READY, MODERATE_GAP, SIGNIFICANT_GAP")
    private String readinessLevel;

    @Schema(description = "Targeted learning, preparation, and course/project recommendations to bridge the gap")
    private List<String> actionableLearningPlan;

    @Schema(description = "Responsible AI notice confirming zero protected characteristic inference")
    private String ethicalNotice = "Evaluation strictly limited to professional skills and job qualifications. Protected characteristics are never used or inferred.";

    private LocalDateTime evaluatedAt = LocalDateTime.now();

    public SkillGapAnalysisDto() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public List<String> getPresentSkills() { return presentSkills; }
    public void setPresentSkills(List<String> presentSkills) { this.presentSkills = presentSkills; }

    public List<String> getLessEvidentSkills() { return lessEvidentSkills; }
    public void setLessEvidentSkills(List<String> lessEvidentSkills) { this.lessEvidentSkills = lessEvidentSkills; }

    public List<String> getMissingRequiredSkills() { return missingRequiredSkills; }
    public void setMissingRequiredSkills(List<String> missingRequiredSkills) { this.missingRequiredSkills = missingRequiredSkills; }

    public List<String> getMissingPreferredSkills() { return missingPreferredSkills; }
    public void setMissingPreferredSkills(List<String> missingPreferredSkills) { this.missingPreferredSkills = missingPreferredSkills; }

    public List<String> getCriticalGaps() { return criticalGaps; }
    public void setCriticalGaps(List<String> criticalGaps) { this.criticalGaps = criticalGaps; }

    public Double getReadinessScore() { return readinessScore; }
    public void setReadinessScore(Double readinessScore) { this.readinessScore = readinessScore; }

    public String getReadinessLevel() { return readinessLevel; }
    public void setReadinessLevel(String readinessLevel) { this.readinessLevel = readinessLevel; }

    public List<String> getActionableLearningPlan() { return actionableLearningPlan; }
    public void setActionableLearningPlan(List<String> actionableLearningPlan) { this.actionableLearningPlan = actionableLearningPlan; }

    public String getEthicalNotice() { return ethicalNotice; }
    public void setEthicalNotice(String ethicalNotice) { this.ethicalNotice = ethicalNotice; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
