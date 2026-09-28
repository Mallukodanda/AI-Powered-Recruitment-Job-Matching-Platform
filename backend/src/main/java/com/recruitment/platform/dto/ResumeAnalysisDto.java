package com.recruitment.platform.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.platform.model.ResumeAnalysis;
import com.recruitment.platform.model.ResumeAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Detailed resume analysis result for candidate review")
public class ResumeAnalysisDto {

    private Long id;
    private Long candidateId;
    private String originalFilename;
    private Long fileSize;
    private ResumeAnalysisStatus status;
    private String aiProvider;
    private String aiModel;
    private String failureReason;
    private StructuredResumeData structuredData;
    private Boolean isReviewed;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public ResumeAnalysisDto() {}

    public static ResumeAnalysisDto fromEntity(ResumeAnalysis entity, ObjectMapper objectMapper) {
        if (entity == null) return null;
        ResumeAnalysisDto dto = new ResumeAnalysisDto();
        dto.setId(entity.getId());
        dto.setCandidateId(entity.getCandidate() != null ? entity.getCandidate().getId() : null);
        dto.setOriginalFilename(entity.getOriginalFilename());
        dto.setFileSize(entity.getFileSize());
        dto.setStatus(entity.getStatus());
        dto.setAiProvider(entity.getAiProvider());
        dto.setAiModel(entity.getAiModel());
        dto.setFailureReason(entity.getFailureReason());
        dto.setIsReviewed(entity.getIsReviewed());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCompletedAt(entity.getCompletedAt());

        if (entity.getStructuredDataJson() != null && !entity.getStructuredDataJson().isBlank()) {
            try {
                StructuredResumeData data = objectMapper.readValue(entity.getStructuredDataJson(), StructuredResumeData.class);
                dto.setStructuredData(data);
            } catch (Exception ignored) {}
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public ResumeAnalysisStatus getStatus() { return status; }
    public void setStatus(ResumeAnalysisStatus status) { this.status = status; }

    public String getAiProvider() { return aiProvider; }
    public void setAiProvider(String aiProvider) { this.aiProvider = aiProvider; }

    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public StructuredResumeData getStructuredData() { return structuredData; }
    public void setStructuredData(StructuredResumeData structuredData) { this.structuredData = structuredData; }

    public Boolean getIsReviewed() { return isReviewed; }
    public void setIsReviewed(Boolean isReviewed) { this.isReviewed = isReviewed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
