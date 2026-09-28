package com.recruitment.platform.dto;

import com.recruitment.platform.model.CandidateSkill;
import com.recruitment.platform.model.ProficiencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Candidate skill detail")
public class CandidateSkillDto {

    private Long id;

    @NotBlank(message = "Skill name is required")
    @Schema(example = "Spring Boot")
    private String name;

    @Schema(example = "BACKEND")
    private String category;

    @Schema(example = "EXPERT")
    private ProficiencyLevel proficiencyLevel = ProficiencyLevel.INTERMEDIATE;

    @Schema(example = "5")
    private Integer yearsExperience;

    public CandidateSkillDto() {}

    public CandidateSkillDto(Long id, String name, String category,
                             ProficiencyLevel proficiencyLevel, Integer yearsExperience) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.proficiencyLevel = proficiencyLevel != null ? proficiencyLevel : ProficiencyLevel.INTERMEDIATE;
        this.yearsExperience = yearsExperience;
    }

    public static CandidateSkillDto fromEntity(CandidateSkill entity) {
        if (entity == null) return null;
        return new CandidateSkillDto(
            entity.getId(),
            entity.getName(),
            entity.getCategory(),
            entity.getProficiencyLevel(),
            entity.getYearsExperience()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public ProficiencyLevel getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }
}
