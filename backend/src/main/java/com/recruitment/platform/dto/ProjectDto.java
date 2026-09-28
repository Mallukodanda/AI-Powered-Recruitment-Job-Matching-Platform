package com.recruitment.platform.dto;

import com.recruitment.platform.model.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Schema(description = "Candidate project detail")
public class ProjectDto {

    private Long id;

    @NotBlank(message = "Project title is required")
    @Schema(example = "Cloud-Native E-Commerce Platform")
    private String title;

    @Schema(example = "Architected a high-throughput microservices store with event sourcing")
    private String description;

    @Schema(example = "Java 21, Spring Boot, React, Docker, Kafka")
    private String technologies;

    @Schema(example = "https://ecommerce.sample.io")
    private String projectUrl;

    @Schema(example = "https://github.com/sample/ecommerce")
    private String repoUrl;

    @Schema(example = "2023-01-01")
    private LocalDate startDate;

    @Schema(example = "2023-06-30")
    private LocalDate endDate;

    public ProjectDto() {}

    public ProjectDto(Long id, String title, String description, String technologies,
                      String projectUrl, String repoUrl, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.technologies = technologies;
        this.projectUrl = projectUrl;
        this.repoUrl = repoUrl;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static ProjectDto fromEntity(Project entity) {
        if (entity == null) return null;
        return new ProjectDto(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getTechnologies(),
            entity.getProjectUrl(),
            entity.getRepoUrl(),
            entity.getStartDate(),
            entity.getEndDate()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTechnologies() { return technologies; }
    public void setTechnologies(String technologies) { this.technologies = technologies; }

    public String getProjectUrl() { return projectUrl; }
    public void setProjectUrl(String projectUrl) { this.projectUrl = projectUrl; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
