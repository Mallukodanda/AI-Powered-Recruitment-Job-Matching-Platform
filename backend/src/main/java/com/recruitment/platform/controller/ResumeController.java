package com.recruitment.platform.controller;

import com.recruitment.platform.dto.CandidateProfileDto;
import com.recruitment.platform.dto.ResumeAnalysisDto;
import com.recruitment.platform.dto.StructuredResumeData;
import com.recruitment.platform.service.ResumeProcessingService;
import com.recruitment.platform.service.ResumeProcessingService.ResumeFileResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resumes")
@Tag(name = "Resume Processing", description = "Secure resume upload, PDF/DOCX text extraction, AI structured analysis, and candidate review synchronization")
@SuppressWarnings("null")
public class ResumeController {

    private final ResumeProcessingService resumeProcessingService;

    public ResumeController(ResumeProcessingService resumeProcessingService) {
        this.resumeProcessingService = resumeProcessingService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Securely upload and analyze a PDF or DOCX resume",
            description = "Validates magic bytes, extension, file size (<=10MB), isolates storage, extracts text, and runs AI structured analysis.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Resume uploaded, extracted, and structured analysis generated"),
            @ApiResponse(responseCode = "400", description = "Validation failure (corrupt file, dangerous extension, magic bytes mismatch, >10MB)"),
            @ApiResponse(responseCode = "403", description = "Unauthorized upload for another candidate"),
            @ApiResponse(responseCode = "404", description = "Candidate profile not found")
    })
    public ResponseEntity<ResumeAnalysisDto> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "candidateId", required = false) Long candidateId,
            Principal principal) {
        String authenticatedEmail = principal != null ? principal.getName() : null;
        ResumeAnalysisDto analysisDto = resumeProcessingService.processResumeUpload(file, candidateId, authenticatedEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(analysisDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed resume analysis results by ID for candidate review")
    public ResponseEntity<ResumeAnalysisDto> getAnalysis(@PathVariable Long id, Principal principal) {
        String authenticatedEmail = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(resumeProcessingService.getAnalysisById(id, authenticatedEmail));
    }

    @GetMapping("/candidate/{candidateId}")
    @Operation(summary = "List all historical resume analyses for a specific candidate")
    public ResponseEntity<List<ResumeAnalysisDto>> getCandidateAnalyses(@PathVariable Long candidateId, Principal principal) {
        String authenticatedEmail = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(resumeProcessingService.getCandidateAnalyses(candidateId, authenticatedEmail));
    }

    @PostMapping("/{id}/apply")
    @Operation(summary = "Apply reviewed and confirmed structured resume data to candidate profile",
            description = "Non-destructively merges skills, experience, education, certifications, and projects into the profile without overwriting existing manual entries.")
    public ResponseEntity<CandidateProfileDto> applyToProfile(
            @PathVariable Long id,
            @RequestBody(required = false) StructuredResumeData confirmedData,
            Principal principal) {
        String authenticatedEmail = principal != null ? principal.getName() : null;
        CandidateProfileDto updatedProfile = resumeProcessingService.applyAnalysisToProfile(id, confirmedData, authenticatedEmail);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Securely download or preview original stored resume file")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long id, Principal principal) throws IOException {
        String authenticatedEmail = principal != null ? principal.getName() : null;
        ResumeFileResource fileResource = resumeProcessingService.loadResumeFile(id, authenticatedEmail);

        String mediaTypeStr = fileResource.contentType() != null ? fileResource.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaTypeStr))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileResource.filename() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileResource.fileSize()))
                .body(fileResource.resource());
    }
}
