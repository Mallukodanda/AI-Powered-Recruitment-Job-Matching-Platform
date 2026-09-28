package com.recruitment.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Multi-criteria filter criteria for authorized recruiter/admin candidate search")
public class CandidateSearchCriteriaDto {

    @Schema(description = "Full-text query over candidate name, headline, bio, and resume text")
    private String query;

    @Schema(description = "Specific skills filter (e.g. ['Java', 'Docker', 'PostgreSQL'])")
    private List<String> skills;

    @Schema(description = "Location filter (e.g. Remote, San Francisco, London)")
    private String location;

    @Schema(description = "Minimum total years of experience")
    private Double minExperienceYears;

    @Schema(description = "Minimum education degree level (e.g. BACHELOR, MASTER, DOCTORATE)")
    private String educationDegree;

    @Schema(description = "Current job title filter (e.g. Senior Software Engineer)")
    private String currentTitle;

    @Schema(description = "Optional natural language query for semantic vector candidate retrieval")
    private String semanticQuery;

    public CandidateSearchCriteriaDto() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getMinExperienceYears() { return minExperienceYears; }
    public void setMinExperienceYears(Double minExperienceYears) { this.minExperienceYears = minExperienceYears; }

    public String getEducationDegree() { return educationDegree; }
    public void setEducationDegree(String educationDegree) { this.educationDegree = educationDegree; }

    public String getCurrentTitle() { return currentTitle; }
    public void setCurrentTitle(String currentTitle) { this.currentTitle = currentTitle; }

    public String getSemanticQuery() { return semanticQuery; }
    public void setSemanticQuery(String semanticQuery) { this.semanticQuery = semanticQuery; }
}
