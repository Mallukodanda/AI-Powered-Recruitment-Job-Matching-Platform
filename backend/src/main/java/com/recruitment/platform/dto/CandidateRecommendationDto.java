package com.recruitment.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Explainable candidate recommendation tailored for an open job requisition")
public class CandidateRecommendationDto {

    private CandidateSummaryDto candidate;

    @Schema(description = "Composite hybrid match score (0.0 to 100.0)")
    private Double matchScore;

    @Schema(description = "Compatibility classification (e.g. STRONG_MATCH, MODERATE_MATCH, POTENTIAL_MATCH)")
    private String matchLevel;

    @Schema(description = "Matched mandatory/required skills")
    private List<String> matchedRequiredSkills;

    @Schema(description = "Missing mandatory skills requiring attention")
    private List<String> missingRequiredSkills;

    @Schema(description = "Verified years of experience")
    private Integer yearsExperience;

    @Schema(description = "Contextual recommendation rationale for the hiring team")
    private String rationale;

    @Schema(description = "Assistive AI disclaimer")
    private String disclaimer = "Assistive candidate recommendation for recruiter review; hiring decisions require human evaluation.";

    public CandidateRecommendationDto() {}

    public CandidateSummaryDto getCandidate() { return candidate; }
    public void setCandidate(CandidateSummaryDto candidate) { this.candidate = candidate; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public String getMatchLevel() { return matchLevel; }
    public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }

    public List<String> getMatchedRequiredSkills() { return matchedRequiredSkills; }
    public void setMatchedRequiredSkills(List<String> matchedRequiredSkills) { this.matchedRequiredSkills = matchedRequiredSkills; }

    public List<String> getMissingRequiredSkills() { return missingRequiredSkills; }
    public void setMissingRequiredSkills(List<String> missingRequiredSkills) { this.missingRequiredSkills = missingRequiredSkills; }

    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
