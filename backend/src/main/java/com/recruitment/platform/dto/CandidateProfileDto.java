package com.recruitment.platform.dto;

import com.recruitment.platform.model.Candidate;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "Comprehensive candidate profile with sub-entities")
public class CandidateProfileDto {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String headline;
    private String currentTitle;
    private Integer yearsExperience;
    private String highestEducation;
    private String skillsSummary;
    private String bio;
    private String resumeText;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private LocalDateTime createdAt;

    private List<EducationDto> educations = new ArrayList<>();
    private List<ExperienceDto> experiences = new ArrayList<>();
    private List<CandidateSkillDto> skills = new ArrayList<>();
    private List<CertificationDto> certifications = new ArrayList<>();
    private List<ProjectDto> projects = new ArrayList<>();

    public CandidateProfileDto() {}

    public static CandidateProfileDto fromEntity(Candidate entity) {
        if (entity == null) return null;
        CandidateProfileDto dto = new CandidateProfileDto();
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setLocation(entity.getLocation());
        dto.setHeadline(entity.getHeadline());
        dto.setCurrentTitle(entity.getCurrentTitle());
        dto.setYearsExperience(entity.getYearsExperience());
        dto.setHighestEducation(entity.getHighestEducation());
        dto.setSkillsSummary(entity.getSkillsSummary());
        dto.setBio(entity.getBio());
        dto.setResumeText(entity.getResumeText());
        dto.setLinkedinUrl(entity.getLinkedinUrl());
        dto.setGithubUrl(entity.getGithubUrl());
        dto.setPortfolioUrl(entity.getPortfolioUrl());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getEducations() != null) {
            dto.setEducations(entity.getEducations().stream().map(EducationDto::fromEntity).collect(Collectors.toList()));
        }
        if (entity.getExperiences() != null) {
            dto.setExperiences(entity.getExperiences().stream().map(ExperienceDto::fromEntity).collect(Collectors.toList()));
        }
        if (entity.getSkills() != null) {
            dto.setSkills(entity.getSkills().stream().map(CandidateSkillDto::fromEntity).collect(Collectors.toList()));
        }
        if (entity.getCertifications() != null) {
            dto.setCertifications(entity.getCertifications().stream().map(CertificationDto::fromEntity).collect(Collectors.toList()));
        }
        if (entity.getProjects() != null) {
            dto.setProjects(entity.getProjects().stream().map(ProjectDto::fromEntity).collect(Collectors.toList()));
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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

    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<EducationDto> getEducations() { return educations; }
    public void setEducations(List<EducationDto> educations) { this.educations = educations; }

    public List<ExperienceDto> getExperiences() { return experiences; }
    public void setExperiences(List<ExperienceDto> experiences) { this.experiences = experiences; }

    public List<CandidateSkillDto> getSkills() { return skills; }
    public void setSkills(List<CandidateSkillDto> skills) { this.skills = skills; }

    public List<CertificationDto> getCertifications() { return certifications; }
    public void setCertifications(List<CertificationDto> certifications) { this.certifications = certifications; }

    public List<ProjectDto> getProjects() { return projects; }
    public void setProjects(List<ProjectDto> projects) { this.projects = projects; }
}
