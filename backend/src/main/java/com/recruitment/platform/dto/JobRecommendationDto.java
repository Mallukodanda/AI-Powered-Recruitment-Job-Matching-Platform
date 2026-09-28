package com.recruitment.platform.dto;

import com.recruitment.platform.model.Job;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Explainable job recommendation tailored to candidate profile and preferences")
public class JobRecommendationDto {

    private Job job;

    @Schema(description = "Composite recommendation match score (0.0 to 100.0)")
    private Double matchScore;

    @Schema(description = "Match level categorization (e.g. STRONG_MATCH, GOOD_MATCH, EXPLORATORY)")
    private String matchLevel;

    @Schema(description = "Signals and positive alignment factors that contributed to this recommendation")
    private List<String> matchingFactors;

    @Schema(description = "Key matched skills")
    private List<String> matchedSkills;

    @Schema(description = "Skills the candidate could develop further for this role")
    private List<String> growthSkills;

    @Schema(description = "Assistive AI disclaimer explicitly stating recommendation is not a guarantee")
    private String disclaimer = "This recommendation is an assistive decision-support suggestion based on profile alignment, not a guarantee of interview or employment.";

    public JobRecommendationDto() {}

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public String getMatchLevel() { return matchLevel; }
    public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }

    public List<String> getMatchingFactors() { return matchingFactors; }
    public void setMatchingFactors(List<String> matchingFactors) { this.matchingFactors = matchingFactors; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getGrowthSkills() { return growthSkills; }
    public void setGrowthSkills(List<String> growthSkills) { this.growthSkills = growthSkills; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
