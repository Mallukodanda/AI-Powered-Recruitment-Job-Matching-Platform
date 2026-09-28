package com.recruitment.platform.dto;

import com.recruitment.platform.model.Candidate;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Compact candidate summary for search and listing")
public class CandidateSummaryDto {

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
    private LocalDateTime createdAt;

    public CandidateSummaryDto() {}

    public CandidateSummaryDto(Long id, String fullName, String email, String phone, String location,
                               String headline, String currentTitle, Integer yearsExperience,
                               String highestEducation, String skillsSummary, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.headline = headline;
        this.currentTitle = currentTitle;
        this.yearsExperience = yearsExperience;
        this.highestEducation = highestEducation;
        this.skillsSummary = skillsSummary;
        this.createdAt = createdAt;
    }

    public static CandidateSummaryDto fromEntity(Candidate entity) {
        if (entity == null) return null;
        return new CandidateSummaryDto(
            entity.getId(),
            entity.getFullName(),
            entity.getEmail(),
            entity.getPhone(),
            entity.getLocation(),
            entity.getHeadline(),
            entity.getCurrentTitle(),
            entity.getYearsExperience(),
            entity.getHighestEducation(),
            entity.getSkillsSummary(),
            entity.getCreatedAt()
        );
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
