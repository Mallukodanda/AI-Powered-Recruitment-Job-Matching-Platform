package com.recruitment.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for updating candidate basic profile details")
public class CandidateProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(example = "Alex Rivera")
    private String fullName;

    @Schema(example = "+1 (555) 234-5678")
    private String phone;

    @Schema(example = "New York, NY / Remote")
    private String location;

    @Schema(example = "Senior Full-Stack Cloud Architect | Spring Boot & React Specialist")
    private String headline;

    @Schema(example = "Lead Software Engineer")
    private String currentTitle;

    @Schema(example = "6")
    private Integer yearsExperience;

    @Schema(example = "M.S. in Computer Science")
    private String highestEducation;

    @Schema(example = "Java, Spring Boot, React, Docker, Kubernetes, AWS")
    private String skillsSummary;

    @Schema(example = "Experienced engineer specializing in cloud-native platforms.")
    private String bio;

    @Schema(example = "https://linkedin.com/in/alex-rivera")
    private String linkedinUrl;

    @Schema(example = "https://github.com/alex-rivera")
    private String githubUrl;

    @Schema(example = "https://alexrivera.dev")
    private String portfolioUrl;

    public CandidateProfileUpdateRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getCurrentTitle() { return currentTitle; }
    public void setCurrentTitle(String currentTitle) { this.currentTitle = currentTitle; }

    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }

    public String getHighestEducation() { return highestEducation; }
    public void setHighestEducation(String highestEducation) { this.highestEducation = highestEducation; }

    public String getSkillsSummary() { return skillsSummary; }
    public void setSkillsSummary(String skillsSummary) { this.skillsSummary = skillsSummary; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
}
