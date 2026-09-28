package com.recruitment.platform.controller;

import com.recruitment.platform.dto.*;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.service.CandidateSearchService;
import com.recruitment.platform.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/candidates")
@Tag(name = "Candidates", description = "Candidate Profiles, Experience, Education, Skills, and Resume Management")
public class CandidateController {

    private final CandidateService candidateService;
    private final CandidateSearchService candidateSearchService;

    public CandidateController(CandidateService candidateService, CandidateSearchService candidateSearchService) {
        this.candidateService = candidateService;
        this.candidateSearchService = candidateSearchService;
    }

    @PostMapping("/search")
    @Operation(summary = "Advanced recruiter candidate search with dynamic filters, sorting, and semantic matching")
    public ResponseEntity<Page<CandidateSummaryDto>> searchCandidates(
            @RequestBody(required = false) CandidateSearchCriteriaDto criteria,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        CandidateSearchCriteriaDto searchCriteria = criteria != null ? criteria : new CandidateSearchCriteriaDto();
        return ResponseEntity.ok(candidateSearchService.searchCandidates(searchCriteria, pageable, email));
    }

    // =========================================================================
    // PROFILE ENDPOINTS
    // =========================================================================

    @GetMapping("/profile/me")
    @Operation(summary = "Get currently authenticated candidate's complete profile")
    public ResponseEntity<CandidateProfileDto> getMyProfile(Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(candidateService.getMyProfile(email));
    }

    @GetMapping("/{id}/profile")
    @Operation(summary = "Get complete candidate profile (Education, Experience, Skills, Certs, Projects)")
    public ResponseEntity<CandidateProfileDto> getCandidateProfile(@PathVariable Long id, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(candidateService.getCandidateProfile(id, email));
    }

    @PutMapping("/{id}/profile")
    @Operation(summary = "Update candidate profile summary and contact details")
    public ResponseEntity<CandidateProfileDto> updateProfile(@PathVariable Long id,
                                                            @Valid @RequestBody CandidateProfileUpdateRequest request,
                                                            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(candidateService.updateCandidateProfile(id, request, email));
    }

    @GetMapping("/paginated")
    @Operation(summary = "Search candidates with pagination and sorting (Recruiter/Admin view)")
    public ResponseEntity<Page<CandidateSummaryDto>> getCandidatesPaginated(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(candidateService.getCandidatesPaginated(search, pageable));
    }

    // =========================================================================
    // EDUCATION SUB-RESOURCE
    // =========================================================================

    @PostMapping("/{candidateId}/education")
    @Operation(summary = "Add an education record to candidate profile")
    public ResponseEntity<EducationDto> addEducation(@PathVariable Long candidateId,
                                                     @Valid @RequestBody EducationDto dto,
                                                     Principal principal) {
        String email = principal != null ? principal.getName() : null;
        EducationDto created = candidateService.addEducation(candidateId, dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{candidateId}/education/{educationId}")
    @Operation(summary = "Update an existing education record")
    public ResponseEntity<EducationDto> updateEducation(@PathVariable Long candidateId,
                                                        @PathVariable Long educationId,
                                                        @Valid @RequestBody EducationDto dto,
                                                        Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(candidateService.updateEducation(candidateId, educationId, dto, email));
    }

    @DeleteMapping("/{candidateId}/education/{educationId}")
    @Operation(summary = "Delete an education record")
    public ResponseEntity<Void> deleteEducation(@PathVariable Long candidateId,
                                                @PathVariable Long educationId,
                                                Principal principal) {
        String email = principal != null ? principal.getName() : null;
        candidateService.deleteEducation(candidateId, educationId, email);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // EXPERIENCE SUB-RESOURCE
    // =========================================================================

    @PostMapping("/{candidateId}/experience")
    @Operation(summary = "Add a work experience record to candidate profile")
    public ResponseEntity<ExperienceDto> addExperience(@PathVariable Long candidateId,
                                                       @Valid @RequestBody ExperienceDto dto,
                                                       Principal principal) {
        String email = principal != null ? principal.getName() : null;
        ExperienceDto created = candidateService.addExperience(candidateId, dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{candidateId}/experience/{experienceId}")
    @Operation(summary = "Update an existing work experience record")
    public ResponseEntity<ExperienceDto> updateExperience(@PathVariable Long candidateId,
                                                          @PathVariable Long experienceId,
                                                          @Valid @RequestBody ExperienceDto dto,
                                                          Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(candidateService.updateExperience(candidateId, experienceId, dto, email));
    }

    @DeleteMapping("/{candidateId}/experience/{experienceId}")
    @Operation(summary = "Delete a work experience record")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long candidateId,
                                                 @PathVariable Long experienceId,
                                                 Principal principal) {
        String email = principal != null ? principal.getName() : null;
        candidateService.deleteExperience(candidateId, experienceId, email);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // SKILLS SUB-RESOURCE
    // =========================================================================

    @PostMapping("/{candidateId}/skills")
    @Operation(summary = "Add or update a skill on candidate profile")
    public ResponseEntity<CandidateSkillDto> addSkill(@PathVariable Long candidateId,
                                                      @Valid @RequestBody CandidateSkillDto dto,
                                                      Principal principal) {
        String email = principal != null ? principal.getName() : null;
        CandidateSkillDto created = candidateService.addSkill(candidateId, dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{candidateId}/skills/{skillId}")
    @Operation(summary = "Delete a skill from candidate profile")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long candidateId,
                                            @PathVariable Long skillId,
                                            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        candidateService.deleteSkill(candidateId, skillId, email);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // CERTIFICATIONS SUB-RESOURCE
    // =========================================================================

    @PostMapping("/{candidateId}/certifications")
    @Operation(summary = "Add a certification to candidate profile")
    public ResponseEntity<CertificationDto> addCertification(@PathVariable Long candidateId,
                                                             @Valid @RequestBody CertificationDto dto,
                                                             Principal principal) {
        String email = principal != null ? principal.getName() : null;
        CertificationDto created = candidateService.addCertification(candidateId, dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{candidateId}/certifications/{certId}")
    @Operation(summary = "Delete a certification from candidate profile")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long candidateId,
                                                    @PathVariable Long certId,
                                                    Principal principal) {
        String email = principal != null ? principal.getName() : null;
        candidateService.deleteCertification(candidateId, certId, email);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // PROJECTS SUB-RESOURCE
    // =========================================================================

    @PostMapping("/{candidateId}/projects")
    @Operation(summary = "Add a portfolio project to candidate profile")
    public ResponseEntity<ProjectDto> addProject(@PathVariable Long candidateId,
                                                 @Valid @RequestBody ProjectDto dto,
                                                 Principal principal) {
        String email = principal != null ? principal.getName() : null;
        ProjectDto created = candidateService.addProject(candidateId, dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{candidateId}/projects/{projectId}")
    @Operation(summary = "Delete a project from candidate profile")
    public ResponseEntity<Void> deleteProject(@PathVariable Long candidateId,
                                              @PathVariable Long projectId,
                                              Principal principal) {
        String email = principal != null ? principal.getName() : null;
        candidateService.deleteProject(candidateId, projectId, email);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // LEGACY & SEARCH ENDPOINTS
    // =========================================================================

    @GetMapping
    @Operation(summary = "List all registered candidates or search by keyword")
    public ResponseEntity<List<Candidate>> getCandidates(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(candidateService.searchCandidates(search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get candidate basic details by ID")
    public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {
        return candidateService.getCandidateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Register candidate profile manually")
    public ResponseEntity<Candidate> createCandidate(@RequestBody Candidate candidate) {
        return ResponseEntity.ok(candidateService.createCandidate(candidate));
    }

    @PostMapping(value = "/upload-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload PDF/DOCX resume file, automatically parse entities and create candidate profile")
    public ResponseEntity<?> uploadResumeFile(@RequestParam("file") MultipartFile file) {
        try {
            Candidate candidate = candidateService.processResumeUpload(file);
            return ResponseEntity.ok(candidate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to parse resume: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/parse-text")
    @Operation(summary = "Submit raw resume text to extract skills and candidate information")
    public ResponseEntity<Candidate> parseResumeText(@RequestBody Map<String, String> payload) {
        String text = payload.get("resumeText");
        String name = payload.get("name");
        String email = payload.get("email");
        Candidate candidate = candidateService.processResumeText(text, name, email);
        return ResponseEntity.ok(candidate);
    }
}
