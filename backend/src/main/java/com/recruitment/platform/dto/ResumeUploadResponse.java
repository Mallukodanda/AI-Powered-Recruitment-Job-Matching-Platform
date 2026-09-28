package com.recruitment.platform.dto;

import com.recruitment.platform.model.ResumeAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Upload confirmation and analysis task status")
public class ResumeUploadResponse {

    private Long analysisId;
    private String originalFilename;
    private Long fileSize;
    private ResumeAnalysisStatus status;
    private String message;
    private LocalDateTime createdAt;

    public ResumeUploadResponse() {}

    public ResumeUploadResponse(Long analysisId, String originalFilename, Long fileSize,
                                ResumeAnalysisStatus status, String message, LocalDateTime createdAt) {
        this.analysisId = analysisId;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getAnalysisId() { return analysisId; }
    public void setAnalysisId(Long analysisId) { this.analysisId = analysisId; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public ResumeAnalysisStatus getStatus() { return status; }
    public void setStatus(ResumeAnalysisStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
