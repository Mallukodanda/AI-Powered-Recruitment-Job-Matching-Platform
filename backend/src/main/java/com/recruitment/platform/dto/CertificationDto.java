package com.recruitment.platform.dto;

import com.recruitment.platform.model.Certification;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Schema(description = "Candidate certification detail")
public class CertificationDto {

    private Long id;

    @NotBlank(message = "Certification name is required")
    @Schema(example = "AWS Certified Solutions Architect - Associate")
    private String name;

    @NotBlank(message = "Issuing organization is required")
    @Schema(example = "Amazon Web Services")
    private String issuingOrganization;

    @Schema(example = "2023-03-15")
    private LocalDate issueDate;

    @Schema(example = "2026-03-15")
    private LocalDate expirationDate;

    @Schema(example = "AWS-SAA-12345678")
    private String credentialId;

    @Schema(example = "https://www.credly.com/badges/sample-id")
    private String credentialUrl;

    public CertificationDto() {}

    public CertificationDto(Long id, String name, String issuingOrganization, LocalDate issueDate,
                            LocalDate expirationDate, String credentialId, String credentialUrl) {
        this.id = id;
        this.name = name;
        this.issuingOrganization = issuingOrganization;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.credentialId = credentialId;
        this.credentialUrl = credentialUrl;
    }

    public static CertificationDto fromEntity(Certification entity) {
        if (entity == null) return null;
        return new CertificationDto(
            entity.getId(),
            entity.getName(),
            entity.getIssuingOrganization(),
            entity.getIssueDate(),
            entity.getExpirationDate(),
            entity.getCredentialId(),
            entity.getCredentialUrl()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIssuingOrganization() { return issuingOrganization; }
    public void setIssuingOrganization(String issuingOrganization) { this.issuingOrganization = issuingOrganization; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getCredentialUrl() { return credentialUrl; }
    public void setCredentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; }
}
