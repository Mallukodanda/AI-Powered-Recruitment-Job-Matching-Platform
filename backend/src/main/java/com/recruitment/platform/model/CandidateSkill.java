package com.recruitment.platform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "candidate_skills", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"candidate_id", "name"})
})
public class CandidateSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(nullable = false)
    private String name;

    private String category; // e.g., BACKEND, FRONTEND, DATABASE, CLOUD

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level")
    private ProficiencyLevel proficiencyLevel = ProficiencyLevel.INTERMEDIATE;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    public CandidateSkill() {}

    public CandidateSkill(Candidate candidate, String name, String category,
                          ProficiencyLevel proficiencyLevel, Integer yearsExperience) {
        this.candidate = candidate;
        this.name = name;
        this.category = category;
        this.proficiencyLevel = proficiencyLevel != null ? proficiencyLevel : ProficiencyLevel.INTERMEDIATE;
        this.yearsExperience = yearsExperience;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public ProficiencyLevel getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }
}
