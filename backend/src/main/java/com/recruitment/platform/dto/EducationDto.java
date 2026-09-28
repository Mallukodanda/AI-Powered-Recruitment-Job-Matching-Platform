package com.recruitment.platform.dto;

import com.recruitment.platform.model.Education;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Schema(description = "Candidate education detail")
public class EducationDto {

    private Long id;

    @NotBlank(message = "Institution name is required")
    @Schema(example = "Stanford University")
    private String institution;

    @NotBlank(message = "Degree title is required")
    @Schema(example = "Bachelor of Science")
    private String degree;

    @Schema(example = "Computer Science")
    private String fieldOfStudy;

    @Schema(example = "2018-09-01")
    private LocalDate startDate;

    @Schema(example = "2022-06-15")
    private LocalDate endDate;

    @Schema(example = "false")
    private Boolean isCurrent = false;

    @Schema(example = "3.9 GPA")
    private String grade;

    @Schema(example = "Graduated with honors, focus on distributed systems")
    private String description;

    public EducationDto() {}

    public EducationDto(Long id, String institution, String degree, String fieldOfStudy,
                        LocalDate startDate, LocalDate endDate, Boolean isCurrent, String grade, String description) {
        this.id = id;
        this.institution = institution;
        this.degree = degree;
        this.fieldOfStudy = fieldOfStudy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent != null ? isCurrent : false;
        this.grade = grade;
        this.description = description;
    }

    public static EducationDto fromEntity(Education entity) {
        if (entity == null) return null;
        return new EducationDto(
            entity.getId(),
            entity.getInstitution(),
            entity.getDegree(),
            entity.getFieldOfStudy(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getIsCurrent(),
            entity.getGrade(),
            entity.getDescription()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getFieldOfStudy() { return fieldOfStudy; }
    public void setFieldOfStudy(String fieldOfStudy) { this.fieldOfStudy = fieldOfStudy; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
