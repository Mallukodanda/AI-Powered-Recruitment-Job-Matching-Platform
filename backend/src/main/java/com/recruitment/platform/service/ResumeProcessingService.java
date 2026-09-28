package com.recruitment.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.platform.dto.*;
import com.recruitment.platform.exception.ResourceNotFoundException;
import com.recruitment.platform.model.*;
import com.recruitment.platform.repository.CandidateRepository;
import com.recruitment.platform.repository.ResumeAnalysisRepository;
import com.recruitment.platform.repository.UserRepository;
import com.recruitment.platform.service.ai.StructuredResumeAnalyzer;
import com.recruitment.platform.service.storage.FileStorageService;
import com.recruitment.platform.service.storage.StoredFileInfo;
import com.recruitment.platform.service.storage.TextExtractionService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class ResumeProcessingService {

    private final FileStorageService fileStorageService;
    private final TextExtractionService textExtractionService;
    private final StructuredResumeAnalyzer structuredResumeAnalyzer;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ResumeProcessingService(FileStorageService fileStorageService,
                                   TextExtractionService textExtractionService,
                                   StructuredResumeAnalyzer structuredResumeAnalyzer,
                                   ResumeAnalysisRepository resumeAnalysisRepository,
                                   CandidateRepository candidateRepository,
                                   CandidateService candidateService,
                                   UserRepository userRepository,
                                   ObjectMapper objectMapper) {
        this.fileStorageService = fileStorageService;
        this.textExtractionService = textExtractionService;
        this.structuredResumeAnalyzer = structuredResumeAnalyzer;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes the secure resume upload and processing pipeline:
     * 1. Authorization & Candidate resolution
     * 2. Deep validation (size, magic bytes, extension, traversal) & Isolated storage
     * 3. Text extraction (PDFBox / Apache POI)
     * 4. AI-assisted structured entity extraction
     * 5. Audit persistence (ResumeAnalysis with PENDING -> EXTRACTED -> ANALYZED / FAILED)
     */
    public ResumeAnalysisDto processResumeUpload(MultipartFile file, Long candidateId, String authenticatedEmail) {
        // Step 1: Candidate resolution & ownership check
        Candidate candidate = resolveCandidate(candidateId, authenticatedEmail);
        checkCandidateOwnership(candidate, authenticatedEmail);

        // Step 2: Secure Storage & Deep Validation (throws FileValidationException on invalid files)
        StoredFileInfo storedInfo = fileStorageService.storeFile(file);

        // Step 3: Record upload attempt in database
        ResumeAnalysis analysis = new ResumeAnalysis(
                candidate,
                storedInfo.originalFilename(),
                storedInfo.storageKey(),
                storedInfo.fileSize(),
                storedInfo.contentType(),
                storedInfo.sha256Hash()
        );
        analysis.setStatus(ResumeAnalysisStatus.PENDING);
        analysis = resumeAnalysisRepository.save(analysis);

        // Step 4: Text Extraction
        String extractedText;
        try (InputStream is = fileStorageService.loadFileStream(storedInfo.storageKey())) {
            extractedText = textExtractionService.extractText(is, storedInfo.originalFilename());
            analysis.setExtractedText(extractedText);
            analysis.setStatus(ResumeAnalysisStatus.EXTRACTED);
        } catch (Exception e) {
            analysis.setStatus(ResumeAnalysisStatus.FAILED);
            analysis.setFailureReason("Text extraction failed: " + e.getMessage());
            analysis.setCompletedAt(LocalDateTime.now());
            analysis = resumeAnalysisRepository.save(analysis);
            return ResumeAnalysisDto.fromEntity(analysis, objectMapper);
        }

        // Step 5: AI-Assisted Structured Resume Analysis (Resilient failure handling)
        try {
            StructuredResumeData structuredData = structuredResumeAnalyzer.analyzeText(extractedText);
            if (structuredData == null) {
                throw new IllegalStateException("AI provider returned empty analysis response");
            }
            String json = objectMapper.writeValueAsString(structuredData);
            analysis.setStructuredDataJson(json);
            analysis.setAiProvider(structuredResumeAnalyzer.getProviderName());
            analysis.setAiModel(structuredResumeAnalyzer.getModelName());
            analysis.setStatus(ResumeAnalysisStatus.ANALYZED);
            analysis.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            // Failure boundary: AI failure must NOT crash the platform or make application unusable
            analysis.setStatus(ResumeAnalysisStatus.FAILED);
            analysis.setAiProvider(structuredResumeAnalyzer.getProviderName());
            analysis.setAiModel(structuredResumeAnalyzer.getModelName());
            analysis.setFailureReason("AI analysis failed: " + e.getMessage());
            analysis.setCompletedAt(LocalDateTime.now());
        }

        analysis = resumeAnalysisRepository.save(analysis);
        return ResumeAnalysisDto.fromEntity(analysis, objectMapper);
    }

    /**
     * Retrieve analysis details by ID for candidate review.
     */
    @Transactional(readOnly = true)
    public ResumeAnalysisDto getAnalysisById(Long analysisId, String authenticatedEmail) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume analysis not found with id: " + analysisId));
        checkCandidateOwnership(analysis.getCandidate(), authenticatedEmail);
        return ResumeAnalysisDto.fromEntity(analysis, objectMapper);
    }

    /**
     * Retrieve all historical resume analyses for a candidate.
     */
    @Transactional(readOnly = true)
    public List<ResumeAnalysisDto> getCandidateAnalyses(Long candidateId, String authenticatedEmail) {
        Candidate candidate = resolveCandidate(candidateId, authenticatedEmail);
        checkCandidateOwnership(candidate, authenticatedEmail);
        return resumeAnalysisRepository.findByCandidateIdOrderByCreatedAtDesc(candidate.getId())
                .stream()
                .map(a -> ResumeAnalysisDto.fromEntity(a, objectMapper))
                .collect(Collectors.toList());
    }

    /**
     * Candidate Review & Explicit Apply Flow:
     * Applies reviewed and confirmed structured resume data into the candidate's active profile.
     * Rule: Do not silently overwrite manually entered information.
     */
    public CandidateProfileDto applyAnalysisToProfile(Long analysisId,
                                                     StructuredResumeData confirmedData,
                                                     String authenticatedEmail) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume analysis not found with id: " + analysisId));

        Candidate candidate = analysis.getCandidate();
        checkCandidateOwnership(candidate, authenticatedEmail);

        if (analysis.getStatus() != ResumeAnalysisStatus.ANALYZED && analysis.getStatus() != ResumeAnalysisStatus.APPLIED) {
            throw new IllegalStateException("Cannot apply resume analysis with status: " + analysis.getStatus() +
                    ". Only successfully ANALYZED resumes can be applied.");
        }

        StructuredResumeData dataToApply = confirmedData;
        if (dataToApply == null && analysis.getStructuredDataJson() != null) {
            try {
                dataToApply = objectMapper.readValue(analysis.getStructuredDataJson(), StructuredResumeData.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read structured resume data: " + e.getMessage(), e);
            }
        }

        if (dataToApply == null) {
            throw new IllegalArgumentException("No structured resume data available to apply.");
        }

        // 1. Candidate basic profile fields (Only populate if blank to protect manual user entries)
        if ((candidate.getCurrentTitle() == null || candidate.getCurrentTitle().isBlank()) && dataToApply.getCurrentTitle() != null) {
            candidate.setCurrentTitle(dataToApply.getCurrentTitle().trim());
        }
        if ((candidate.getHeadline() == null || candidate.getHeadline().isBlank()) && dataToApply.getCurrentTitle() != null) {
            candidate.setHeadline(dataToApply.getCurrentTitle().trim());
        }
        if ((candidate.getYearsExperience() == null || candidate.getYearsExperience() == 0) && dataToApply.getYearsExperience() != null) {
            candidate.setYearsExperience(dataToApply.getYearsExperience());
        }
        if ((candidate.getBio() == null || candidate.getBio().isBlank()) && dataToApply.getSummary() != null) {
            candidate.setBio(dataToApply.getSummary().trim());
        }
        if ((candidate.getPhone() == null || candidate.getPhone().isBlank()) && dataToApply.getPhone() != null) {
            candidate.setPhone(dataToApply.getPhone().trim());
        }
        if ((candidate.getLocation() == null || candidate.getLocation().isBlank()) && dataToApply.getLocation() != null) {
            candidate.setLocation(dataToApply.getLocation().trim());
        }

        // Always store latest extracted text on the candidate profile
        if (analysis.getExtractedText() != null) {
            candidate.setResumeText(analysis.getExtractedText());
        }
        candidateRepository.save(candidate);

        // 2. Append confirmed Skills
        if (dataToApply.getSkills() != null) {
            for (String skillName : dataToApply.getSkills()) {
                if (skillName != null && !skillName.isBlank()) {
                    candidateService.addSkill(candidate.getId(),
                            new CandidateSkillDto(null, skillName.trim(), "Extracted", ProficiencyLevel.INTERMEDIATE, 2),
                            authenticatedEmail);
                }
            }
        }

        // 3. Append confirmed Education
        if (dataToApply.getEducation() != null) {
            for (EducationDto edu : dataToApply.getEducation()) {
                if (edu.getInstitution() != null && !edu.getInstitution().isBlank()) {
                    candidateService.addEducation(candidate.getId(), edu, authenticatedEmail);
                }
            }
        }

        // 4. Append confirmed Experience
        if (dataToApply.getExperience() != null) {
            for (ExperienceDto exp : dataToApply.getExperience()) {
                if (exp.getCompany() != null && !exp.getCompany().isBlank()) {
                    if (exp.getStartDate() == null) {
                        exp.setStartDate(LocalDate.now().minusYears(2));
                    }
                    candidateService.addExperience(candidate.getId(), exp, authenticatedEmail);
                }
            }
        }

        // 5. Append confirmed Certifications
        if (dataToApply.getCertifications() != null) {
            for (CertificationDto cert : dataToApply.getCertifications()) {
                if (cert.getName() != null && !cert.getName().isBlank()) {
                    candidateService.addCertification(candidate.getId(), cert, authenticatedEmail);
                }
            }
        }

        // 6. Append confirmed Projects
        if (dataToApply.getProjects() != null) {
            for (ProjectDto proj : dataToApply.getProjects()) {
                if (proj.getTitle() != null && !proj.getTitle().isBlank()) {
                    candidateService.addProject(candidate.getId(), proj, authenticatedEmail);
                }
            }
        }

        // Mark analysis record as reviewed & applied
        analysis.setIsReviewed(true);
        analysis.setStatus(ResumeAnalysisStatus.APPLIED);
        resumeAnalysisRepository.save(analysis);

        return candidateService.getCandidateProfile(candidate.getId(), authenticatedEmail);
    }

    /**
     * Load stored resume file stream for secure preview or download.
     */
    @Transactional(readOnly = true)
    public ResumeFileResource loadResumeFile(Long analysisId, String authenticatedEmail) throws IOException {
        ResumeAnalysis analysis = resumeAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume analysis not found with id: " + analysisId));
        checkCandidateOwnership(analysis.getCandidate(), authenticatedEmail);

        InputStream inputStream = fileStorageService.loadFileStream(analysis.getStorageKey());
        Resource resource = new InputStreamResource(inputStream);
        return new ResumeFileResource(resource, analysis.getOriginalFilename(), analysis.getContentType(), analysis.getFileSize());
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private Candidate resolveCandidate(Long candidateId, String authenticatedEmail) {
        if (candidateId != null) {
            return candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found with id: " + candidateId));
        } else if (authenticatedEmail != null && !authenticatedEmail.isBlank()) {
            return candidateRepository.findByEmailIgnoreCase(authenticatedEmail)
                    .orElseGet(() -> {
                        User user = userRepository.findByEmail(authenticatedEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authenticatedEmail));
                        Candidate newCandidate = new Candidate(user, user.getFullName(), user.getEmail(), null, null, null);
                        return candidateRepository.save(newCandidate);
                    });
        } else {
            // In unauthenticated local/test environments when no candidate ID is passed, check for existing candidate
            return candidateRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Candidate ID or authenticated candidate context is required"));
        }
    }

    private void checkCandidateOwnership(Candidate candidate, String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) return;

        User authUser = userRepository.findByEmail(authenticatedEmail).orElse(null);
        if (authUser == null) return;
        if (authUser.getRole() == Role.ADMIN) return;

        boolean matchesEmail = candidate.getEmail().equalsIgnoreCase(authenticatedEmail);
        boolean matchesUserId = candidate.getUser() != null && candidate.getUser().getId().equals(authUser.getId());

        if (!matchesEmail && !matchesUserId) {
            throw new AccessDeniedException("Access denied: You do not have permission to access or modify this resume.");
        }
    }

    public record ResumeFileResource(Resource resource, String filename, String contentType, Long fileSize) {}
}
