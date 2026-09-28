package com.recruitment.platform.dto;

import com.recruitment.platform.model.Experience;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Candidate professional work experience")
public class ExperienceDto {

    private Long id;

    @NotBlank(message = "Company name is required")
    @Schema(example = "Google Inc.")
    private String company;

    @NotBlank(message = "Job title is required")
    @Schema(example = "Senior Software Engineer")
    private String title;

    @Schema(example = "Mountain View, CA / Hybrid")
    private String location;

    @Schema(example = "FULL_TIME")
    private String employmentType;

    @NotNull(message = "Start date is required")
    @Schema(example = "2022-07-01")
    private LocalDate startDate;

    @Schema(example = "2024-05-01")
    private LocalDate endDate;

    @Schema(example = "false")
    private Boolean isCurrent = false;

    @Schema(example = "Designed high-throughput microservices using Spring Boot and Kafka.")
    private String description;

    public ExperienceDto() {}

    public ExperienceDto(Long id, String company, String title, String location, String employmentType,
                         LocalDate startDate, LocalDate endDate, Boolean isCurrent, String description) {
        this.id = id;
        this.company = company;
        this.title = title;
        this.location = location;
        this.employmentType = employmentType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent != null ? isCurrent : false;
        this.description = description;
    }

    public static ExperienceDto fromEntity(Experience entity) {
        if (entity == null) return null;
        return new ExperienceDto(
            entity.getId(),
            entity.getCompany(),
            entity.getTitle(),
            entity.getLocation(),
            entity.getEmploymentType(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getIsCurrent(),
            entity.getDescription()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
