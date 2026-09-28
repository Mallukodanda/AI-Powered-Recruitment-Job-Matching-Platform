package com.recruitment.platform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String location;

    private String headline;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "current_title")
    private String currentTitle;

    @Column(name = "years_experience")
    private Integer yearsExperience = 0;

    @Column(name = "highest_education")
    private String highestEducation;

    @Lob
    @Column(name = "skills_summary", columnDefinition = "TEXT")
    private String skillsSummary; // Comma-separated or extracted skills

    @Lob
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Lob
    @Column(name = "resume_text", columnDefinition = "TEXT")
    private String resumeText;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CandidateSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    @OneToOne(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private CandidateEmbedding candidateEmbedding;

    public Candidate() {}

    public Candidate(String fullName, String email, String phone, String currentTitle, Integer yearsExperience, String skillsSummary) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.currentTitle = currentTitle;
        this.yearsExperience = yearsExperience;
        this.skillsSummary = skillsSummary;
        this.createdAt = LocalDateTime.now();
    }

    public Candidate(User user, String fullName, String email, String phone, String location, String headline) {
        this.user = user;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.headline = headline;
        this.createdAt = LocalDateTime.now();
    }

    // Helper methods for bidirectional relationships
    public void addEducation(Education education) {
        educations.add(education);
        education.setCandidate(this);
    }

    public void removeEducation(Education education) {
        educations.remove(education);
        education.setCandidate(null);
    }

    public void addExperience(Experience experience) {
        experiences.add(experience);
        experience.setCandidate(this);
    }

    public void removeExperience(Experience experience) {
        experiences.remove(experience);
        experience.setCandidate(null);
    }

    public void addSkill(CandidateSkill skill) {
        skills.add(skill);
        skill.setCandidate(this);
    }

    public void removeSkill(CandidateSkill skill) {
        skills.remove(skill);
        skill.setCandidate(null);
    }

    public void addCertification(Certification cert) {
        certifications.add(cert);
        cert.setCandidate(this);
    }

    public void removeCertification(Certification cert) {
        certifications.remove(cert);
        cert.setCandidate(null);
    }

    public void addProject(Project project) {
        projects.add(project);
        project.setCandidate(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.setCandidate(null);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Education> getEducations() { return educations; }
    public void setEducations(List<Education> educations) { this.educations = educations; }

    public List<Experience> getExperiences() { return experiences; }
    public void setExperiences(List<Experience> experiences) { this.experiences = experiences; }

    public List<CandidateSkill> getSkills() { return skills; }
    public void setSkills(List<CandidateSkill> skills) { this.skills = skills; }

    public List<Certification> getCertifications() { return certifications; }
    public void setCertifications(List<Certification> certifications) { this.certifications = certifications; }

    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }

    public CandidateEmbedding getCandidateEmbedding() { return candidateEmbedding; }
    public void setCandidateEmbedding(CandidateEmbedding candidateEmbedding) { this.candidateEmbedding = candidateEmbedding; }
}
