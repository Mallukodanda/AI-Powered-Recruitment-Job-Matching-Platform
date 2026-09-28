package com.recruitment.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Structured entity output extracted from job description")
public class StructuredJobData {

    private String jobTitle;
    private String roleSummary;
    private List<String> requiredSkills = new ArrayList<>();
    private List<String> preferredSkills = new ArrayList<>();
    private List<String> technologies = new ArrayList<>();
    private Integer minExperienceYears;
    private String targetExperienceLevel;
    private String educationRequirement;
    private List<String> responsibilities = new ArrayList<>();
    private List<String> importantRequirements = new ArrayList<>();

    public StructuredJobData() {}

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getRoleSummary() { return roleSummary; }
    public void setRoleSummary(String roleSummary) { this.roleSummary = roleSummary; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>(); }

    public List<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<String> preferredSkills) { this.preferredSkills = preferredSkills != null ? preferredSkills : new ArrayList<>(); }

    public List<String> getTechnologies() { return technologies; }
    public void setTechnologies(List<String> technologies) { this.technologies = technologies != null ? technologies : new ArrayList<>(); }

    public Integer getMinExperienceYears() { return minExperienceYears; }
    public void setMinExperienceYears(Integer minExperienceYears) { this.minExperienceYears = minExperienceYears; }

    public String getTargetExperienceLevel() { return targetExperienceLevel; }
    public void setTargetExperienceLevel(String targetExperienceLevel) { this.targetExperienceLevel = targetExperienceLevel; }

    public String getEducationRequirement() { return educationRequirement; }
    public void setEducationRequirement(String educationRequirement) { this.educationRequirement = educationRequirement; }

    public List<String> getResponsibilities() { return responsibilities; }
    public void setResponsibilities(List<String> responsibilities) { this.responsibilities = responsibilities != null ? responsibilities : new ArrayList<>(); }

    public List<String> getImportantRequirements() { return importantRequirements; }
    public void setImportantRequirements(List<String> importantRequirements) { this.importantRequirements = importantRequirements != null ? importantRequirements : new ArrayList<>(); }
}
