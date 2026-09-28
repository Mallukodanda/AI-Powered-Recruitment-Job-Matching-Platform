package com.recruitment.platform.dto;

import java.util.ArrayList;
import java.util.List;

public class MatchScoreResponse {

    private double overallScore;
    private double skillScore;
    private double experienceScore;
    private double educationScore;
    private double semanticScore;

    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private List<String> bonusSkills = new ArrayList<>();

    private String suitabilityLevel;
    private String recommendationRationale;
    private List<String> recommendedNextSteps = new ArrayList<>();

    public MatchScoreResponse() {}

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public double getSkillScore() { return skillScore; }
    public void setSkillScore(double skillScore) { this.skillScore = skillScore; }

    public double getExperienceScore() { return experienceScore; }
    public void setExperienceScore(double experienceScore) { this.experienceScore = experienceScore; }

    public double getEducationScore() { return educationScore; }
    public void setEducationScore(double educationScore) { this.educationScore = educationScore; }

    public double getSemanticScore() { return semanticScore; }
    public void setSemanticScore(double semanticScore) { this.semanticScore = semanticScore; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getBonusSkills() { return bonusSkills; }
    public void setBonusSkills(List<String> bonusSkills) { this.bonusSkills = bonusSkills; }

    public String getSuitabilityLevel() { return suitabilityLevel; }
    public void setSuitabilityLevel(String suitabilityLevel) { this.suitabilityLevel = suitabilityLevel; }

    public String getRecommendationRationale() { return recommendationRationale; }
    public void setRecommendationRationale(String recommendationRationale) { this.recommendationRationale = recommendationRationale; }

    public List<String> getRecommendedNextSteps() { return recommendedNextSteps; }
    public void setRecommendedNextSteps(List<String> recommendedNextSteps) { this.recommendedNextSteps = recommendedNextSteps; }
}
