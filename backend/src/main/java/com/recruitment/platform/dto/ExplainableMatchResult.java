package com.recruitment.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Explainable evidence-based matching output for human hiring team review")
public class ExplainableMatchResult {

    private double overallScore;
    private String compatibilityBand;

    // Component signal scores
    private double requiredSkillScore;
    private double semanticScore;
    private double preferredSkillScore;
    private double experienceScore;
    private double educationScore;
    private double technologyScore;

    // Evidence
    private List<String> matchedRequiredSkills = new ArrayList<>();
    private List<String> missingRequiredSkills = new ArrayList<>();
    private List<String> matchedPreferredSkills = new ArrayList<>();
    private List<String> relevantTechnologies = new ArrayList<>();

    // Alignment details
    private Integer candidateExperienceYears;
    private Integer requiredExperienceYears;
    private String experienceAlignment;
    private String educationAlignment;
    private List<String> requirementAlignmentNotes = new ArrayList<>();

    // Assistive decision support
    private String decisionSupportSummary;

    public ExplainableMatchResult() {
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public String getCompatibilityBand() {
        return compatibilityBand;
    }

    public void setCompatibilityBand(String compatibilityBand) {
        this.compatibilityBand = compatibilityBand;
    }

    public double getRequiredSkillScore() {
        return requiredSkillScore;
    }

    public void setRequiredSkillScore(double requiredSkillScore) {
        this.requiredSkillScore = requiredSkillScore;
    }

    public double getSemanticScore() {
        return semanticScore;
    }

    public void setSemanticScore(double semanticScore) {
        this.semanticScore = semanticScore;
    }

    public double getPreferredSkillScore() {
        return preferredSkillScore;
    }

    public void setPreferredSkillScore(double preferredSkillScore) {
        this.preferredSkillScore = preferredSkillScore;
    }

    public double getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(double experienceScore) {
        this.experienceScore = experienceScore;
    }

    public double getEducationScore() {
        return educationScore;
    }

    public void setEducationScore(double educationScore) {
        this.educationScore = educationScore;
    }

    public double getTechnologyScore() {
        return technologyScore;
    }

    public void setTechnologyScore(double technologyScore) {
        this.technologyScore = technologyScore;
    }

    public List<String> getMatchedRequiredSkills() {
        return matchedRequiredSkills;
    }

    public void setMatchedRequiredSkills(List<String> matchedRequiredSkills) {
        this.matchedRequiredSkills = matchedRequiredSkills != null ? matchedRequiredSkills : new ArrayList<>();
    }

    public List<String> getMissingRequiredSkills() {
        return missingRequiredSkills;
    }

    public void setMissingRequiredSkills(List<String> missingRequiredSkills) {
        this.missingRequiredSkills = missingRequiredSkills != null ? missingRequiredSkills : new ArrayList<>();
    }

    public List<String> getMatchedPreferredSkills() {
        return matchedPreferredSkills;
    }

    public void setMatchedPreferredSkills(List<String> matchedPreferredSkills) {
        this.matchedPreferredSkills = matchedPreferredSkills != null ? matchedPreferredSkills : new ArrayList<>();
    }

    public List<String> getRelevantTechnologies() {
        return relevantTechnologies;
    }

    public void setRelevantTechnologies(List<String> relevantTechnologies) {
        this.relevantTechnologies = relevantTechnologies != null ? relevantTechnologies : new ArrayList<>();
    }

    public Integer getCandidateExperienceYears() {
        return candidateExperienceYears;
    }

    public void setCandidateExperienceYears(Integer candidateExperienceYears) {
        this.candidateExperienceYears = candidateExperienceYears;
    }

    public Integer getRequiredExperienceYears() {
        return requiredExperienceYears;
    }

    public void setRequiredExperienceYears(Integer requiredExperienceYears) {
        this.requiredExperienceYears = requiredExperienceYears;
    }

    public String getExperienceAlignment() {
        return experienceAlignment;
    }

    public void setExperienceAlignment(String experienceAlignment) {
        this.experienceAlignment = experienceAlignment;
    }

    public String getEducationAlignment() {
        return educationAlignment;
    }

    public void setEducationAlignment(String educationAlignment) {
        this.educationAlignment = educationAlignment;
    }

    public List<String> getRequirementAlignmentNotes() {
        return requirementAlignmentNotes;
    }

    public void setRequirementAlignmentNotes(List<String> requirementAlignmentNotes) {
        this.requirementAlignmentNotes = requirementAlignmentNotes != null ? requirementAlignmentNotes
                : new ArrayList<>();
    }

    public String getDecisionSupportSummary() {
        return decisionSupportSummary;
    }

    public void setDecisionSupportSummary(String decisionSupportSummary) {
        this.decisionSupportSummary = decisionSupportSummary;
    }
}
