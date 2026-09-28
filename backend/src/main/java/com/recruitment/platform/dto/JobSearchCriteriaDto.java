package com.recruitment.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Multi-criteria filter criteria for advanced job search and semantic discovery")
public class JobSearchCriteriaDto {

    @Schema(description = "Keyword search over title, description, requirements, and skills")
    private String query;

    @Schema(description = "Department filter (e.g. Engineering, Product, Design)")
    private String department;

    @Schema(description = "Location filter (e.g. Remote, San Francisco, New York)")
    private String location;

    @Schema(description = "Employment type filter (e.g. FULL_TIME, PART_TIME, CONTRACT)")
    private String jobType;

    @Schema(description = "Seniority/Experience level (e.g. ENTRY, MID, SENIOR, LEAD)")
    private String experienceLevel;

    @Schema(description = "Minimum years of experience required")
    private Integer minExperienceYears;

    @Schema(description = "Maximum years of experience required")
    private Integer maxExperienceYears;

    @Schema(description = "Job status filter (default: ACTIVE)")
    private String status = "ACTIVE";

    @Schema(description = "Optional natural language query for dense semantic vector similarity search")
    private String semanticQuery;

    public JobSearchCriteriaDto() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public Integer getMinExperienceYears() { return minExperienceYears; }
    public void setMinExperienceYears(Integer minExperienceYears) { this.minExperienceYears = minExperienceYears; }

    public Integer getMaxExperienceYears() { return maxExperienceYears; }
    public void setMaxExperienceYears(Integer maxExperienceYears) { this.maxExperienceYears = maxExperienceYears; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSemanticQuery() { return semanticQuery; }
    public void setSemanticQuery(String semanticQuery) { this.semanticQuery = semanticQuery; }
}
