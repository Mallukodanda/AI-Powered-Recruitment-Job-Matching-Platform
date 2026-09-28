package com.recruitment.platform.service;

import com.recruitment.platform.dto.*;
import com.recruitment.platform.exception.ResourceNotFoundException;
import com.recruitment.platform.model.*;
import com.recruitment.platform.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@SuppressWarnings("null")
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final CertificationRepository certificationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ResumeParserService resumeParserService;

    public CandidateService(CandidateRepository candidateRepository,
                            EducationRepository educationRepository,
                            ExperienceRepository experienceRepository,
                            CandidateSkillRepository candidateSkillRepository,
                            CertificationRepository certificationRepository,
                            ProjectRepository projectRepository,
                            UserRepository userRepository,
                            ResumeParserService resumeParserService) {
        this.candidateRepository = candidateRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.certificationRepository = certificationRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.resumeParserService = resumeParserService;
    }

    // =========================================================================
    // CANDIDATE PROFILE (Comprehensive)
    // =========================================================================

    @Transactional(readOnly = true)
    public CandidateProfileDto getMyProfile(String authenticatedEmail) {
        Candidate candidate = candidateRepository.findByEmailIgnoreCase(authenticatedEmail)
                .orElseGet(() -> {
                    // Auto-initialize candidate profile for registered user if not yet created
                    User user = userRepository.findByEmail(authenticatedEmail)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authenticatedEmail));
                    Candidate newCandidate = new Candidate(user, user.getFullName(), user.getEmail(), null, null, null);
                    return candidateRepository.save(newCandidate);
                });

        return assembleProfileDto(candidate);
    }

    @Transactional(readOnly = true)
    public CandidateProfileDto getCandidateProfile(Long candidateId, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, true);
        return assembleProfileDto(candidate);
    }

    public CandidateProfileDto updateCandidateProfile(Long candidateId, CandidateProfileUpdateRequest request, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        if (request.getFullName() != null) candidate.setFullName(request.getFullName().trim());
        if (request.getPhone() != null) candidate.setPhone(request.getPhone().trim());
        if (request.getLocation() != null) candidate.setLocation(request.getLocation().trim());
        if (request.getHeadline() != null) candidate.setHeadline(request.getHeadline().trim());
        if (request.getCurrentTitle() != null) candidate.setCurrentTitle(request.getCurrentTitle().trim());
        if (request.getYearsExperience() != null) candidate.setYearsExperience(request.getYearsExperience());
        if (request.getHighestEducation() != null) candidate.setHighestEducation(request.getHighestEducation().trim());
        if (request.getSkillsSummary() != null) candidate.setSkillsSummary(request.getSkillsSummary().trim());
        if (request.getBio() != null) candidate.setBio(request.getBio().trim());
        if (request.getLinkedinUrl() != null) candidate.setLinkedinUrl(request.getLinkedinUrl().trim());
        if (request.getGithubUrl() != null) candidate.setGithubUrl(request.getGithubUrl().trim());
        if (request.getPortfolioUrl() != null) candidate.setPortfolioUrl(request.getPortfolioUrl().trim());

        Candidate saved = candidateRepository.save(candidate);
        return assembleProfileDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<CandidateSummaryDto> getCandidatesPaginated(String search, Pageable pageable) {
        Page<Candidate> candidates = candidateRepository.searchCandidatesPaginated(search, pageable);
        return candidates.map(CandidateSummaryDto::fromEntity);
    }

    // =========================================================================
    // EDUCATION MANAGEMENT
    // =========================================================================

    public EducationDto addEducation(Long candidateId, EducationDto dto, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        Education education = new Education(
                candidate,
                dto.getInstitution().trim(),
                dto.getDegree().trim(),
                dto.getFieldOfStudy() != null ? dto.getFieldOfStudy().trim() : null,
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getIsCurrent(),
                dto.getGrade() != null ? dto.getGrade().trim() : null,
                dto.getDescription() != null ? dto.getDescription().trim() : null
        );

        Education saved = educationRepository.save(education);
        return EducationDto.fromEntity(saved);
    }

    public EducationDto updateEducation(Long candidateId, Long educationId, EducationDto dto, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        Education education = educationRepository.findByIdAndCandidateId(educationId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Education record not found with id: " + educationId));

        education.setInstitution(dto.getInstitution().trim());
        education.setDegree(dto.getDegree().trim());
        education.setFieldOfStudy(dto.getFieldOfStudy() != null ? dto.getFieldOfStudy().trim() : null);
        education.setStartDate(dto.getStartDate());
        education.setEndDate(dto.getEndDate());
        education.setIsCurrent(dto.getIsCurrent() != null ? dto.getIsCurrent() : false);
        education.setGrade(dto.getGrade() != null ? dto.getGrade().trim() : null);
        education.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);

        Education saved = educationRepository.save(education);
        return EducationDto.fromEntity(saved);
    }

    public void deleteEducation(Long candidateId, Long educationId, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        Education education = educationRepository.findByIdAndCandidateId(educationId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Education record not found with id: " + educationId));

        educationRepository.delete(education);
    }

    // =========================================================================
    // EXPERIENCE MANAGEMENT
    // =========================================================================

    public ExperienceDto addExperience(Long candidateId, ExperienceDto dto, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        Experience experience = new Experience(
                candidate,
                dto.getCompany().trim(),
                dto.getTitle().trim(),
                dto.getLocation() != null ? dto.getLocation().trim() : null,
                dto.getEmploymentType() != null ? dto.getEmploymentType().trim() : null,
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getIsCurrent(),
                dto.getDescription() != null ? dto.getDescription().trim() : null
        );

        Experience saved = experienceRepository.save(experience);
        return ExperienceDto.fromEntity(saved);
    }

    public ExperienceDto updateExperience(Long candidateId, Long experienceId, ExperienceDto dto, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        Experience experience = experienceRepository.findByIdAndCandidateId(experienceId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience record not found with id: " + experienceId));

        experience.setCompany(dto.getCompany().trim());
        experience.setTitle(dto.getTitle().trim());
        experience.setLocation(dto.getLocation() != null ? dto.getLocation().trim() : null);
        experience.setEmploymentType(dto.getEmploymentType() != null ? dto.getEmploymentType().trim() : null);
        experience.setStartDate(dto.getStartDate());
        experience.setEndDate(dto.getEndDate());
        experience.setIsCurrent(dto.getIsCurrent() != null ? dto.getIsCurrent() : false);
        experience.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);

        Experience saved = experienceRepository.save(experience);
        return ExperienceDto.fromEntity(saved);
    }

    public void deleteExperience(Long candidateId, Long experienceId, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        Experience experience = experienceRepository.findByIdAndCandidateId(experienceId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience record not found with id: " + experienceId));

        experienceRepository.delete(experience);
    }

    // =========================================================================
    // SKILLS MANAGEMENT
    // =========================================================================

    public CandidateSkillDto addSkill(Long candidateId, CandidateSkillDto dto, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        String trimmedName = dto.getName().trim();
        Optional<CandidateSkill> existing = candidateSkillRepository.findByCandidateIdAndNameIgnoreCase(candidateId, trimmedName);
        if (existing.isPresent()) {
            CandidateSkill skill = existing.get();
            skill.setCategory(dto.getCategory() != null ? dto.getCategory().trim() : skill.getCategory());
            skill.setProficiencyLevel(dto.getProficiencyLevel() != null ? dto.getProficiencyLevel() : skill.getProficiencyLevel());
            skill.setYearsExperience(dto.getYearsExperience() != null ? dto.getYearsExperience() : skill.getYearsExperience());
            return CandidateSkillDto.fromEntity(candidateSkillRepository.save(skill));
        }

        CandidateSkill skill = new CandidateSkill(
                candidate,
                trimmedName,
                dto.getCategory() != null ? dto.getCategory().trim() : null,
                dto.getProficiencyLevel(),
                dto.getYearsExperience()
        );

        CandidateSkill saved = candidateSkillRepository.save(skill);
        return CandidateSkillDto.fromEntity(saved);
    }

    public void deleteSkill(Long candidateId, Long skillId, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        CandidateSkill skill = candidateSkillRepository.findByIdAndCandidateId(skillId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill record not found with id: " + skillId));

        candidateSkillRepository.delete(skill);
    }

    // =========================================================================
    // CERTIFICATIONS MANAGEMENT
    // =========================================================================

    public CertificationDto addCertification(Long candidateId, CertificationDto dto, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        Certification cert = new Certification(
                candidate,
                dto.getName().trim(),
                dto.getIssuingOrganization().trim(),
                dto.getIssueDate(),
                dto.getExpirationDate(),
                dto.getCredentialId() != null ? dto.getCredentialId().trim() : null,
                dto.getCredentialUrl() != null ? dto.getCredentialUrl().trim() : null
        );

        Certification saved = certificationRepository.save(cert);
        return CertificationDto.fromEntity(saved);
    }

    public void deleteCertification(Long candidateId, Long certId, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        Certification cert = certificationRepository.findByIdAndCandidateId(certId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + certId));

        certificationRepository.delete(cert);
    }

    // =========================================================================
    // PROJECTS MANAGEMENT
    // =========================================================================

    public ProjectDto addProject(Long candidateId, ProjectDto dto, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        Project project = new Project(
                candidate,
                dto.getTitle().trim(),
                dto.getDescription() != null ? dto.getDescription().trim() : null,
                dto.getTechnologies() != null ? dto.getTechnologies().trim() : null,
                dto.getProjectUrl() != null ? dto.getProjectUrl().trim() : null,
                dto.getRepoUrl() != null ? dto.getRepoUrl().trim() : null,
                dto.getStartDate(),
                dto.getEndDate()
        );

        Project saved = projectRepository.save(project);
        return ProjectDto.fromEntity(saved);
    }

    public void deleteProject(Long candidateId, Long projectId, String authenticatedEmail) {
        Candidate candidate = findCandidateOrThrow(candidateId);
        checkOwnershipOrRole(candidate, authenticatedEmail, false);

        Project project = projectRepository.findByIdAndCandidateId(projectId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        projectRepository.delete(project);
    }

    // =========================================================================
    // HELPER METHODS (N+1 query prevention & Ownership checks)
    // =========================================================================

    private Candidate findCandidateOrThrow(Long candidateId) {
        return candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found with id: " + candidateId));
    }

    private CandidateProfileDto assembleProfileDto(Candidate candidate) {
        Long candidateId = candidate.getId();
        CandidateProfileDto dto = CandidateProfileDto.fromEntity(candidate);

        // Load collections using dedicated indexed queries to prevent N+1 and Cartesian product
        dto.setEducations(educationRepository.findByCandidateIdOrderByStartDateDesc(candidateId)
                .stream().map(EducationDto::fromEntity).toList());
        dto.setExperiences(experienceRepository.findByCandidateIdOrderByStartDateDesc(candidateId)
                .stream().map(ExperienceDto::fromEntity).toList());
        dto.setSkills(candidateSkillRepository.findByCandidateIdOrderByNameAsc(candidateId)
                .stream().map(CandidateSkillDto::fromEntity).toList());
        dto.setCertifications(certificationRepository.findByCandidateIdOrderByIssueDateDesc(candidateId)
                .stream().map(CertificationDto::fromEntity).toList());
        dto.setProjects(projectRepository.findByCandidateIdOrderByStartDateDesc(candidateId)
                .stream().map(ProjectDto::fromEntity).toList());

        return dto;
    }

    private void checkOwnershipOrRole(Candidate candidate, String authenticatedEmail, boolean allowRecruiterView) {
        if (authenticatedEmail == null) return; // Allow internal or test calls without security context

        User authUser = userRepository.findByEmail(authenticatedEmail).orElse(null);
        if (authUser == null) return;

        // Admin has universal access
        if (authUser.getRole() == Role.ADMIN) return;

        // Recruiter can view profiles (read-only)
        if (allowRecruiterView && authUser.getRole() == Role.RECRUITER) return;

        // Candidate can only view and mutate their own profile
        boolean matchesEmail = candidate.getEmail().equalsIgnoreCase(authenticatedEmail);
        boolean matchesUserId = candidate.getUser() != null && candidate.getUser().getId().equals(authUser.getId());

        if (!matchesEmail && !matchesUserId) {
            throw new AccessDeniedException("Access denied: You do not have permission to access or modify this candidate profile.");
        }
    }

    // =========================================================================
    // LEGACY & BACKWARD COMPATIBLE METHODS
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Candidate> getCandidateById(Long id) {
        return candidateRepository.findById(id);
    }

    public Candidate createCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    public Candidate processResumeUpload(MultipartFile file) throws Exception {
        ResumeParseResult parsed = resumeParserService.parseResumeFile(file);

        String email = parsed.getExtractedEmail() != null ? parsed.getExtractedEmail() : "candidate-" + System.currentTimeMillis() + "@talent.io";
        Candidate candidate = candidateRepository.findByEmail(email).orElse(new Candidate());

        candidate.setFullName(parsed.getExtractedName() != null ? parsed.getExtractedName() : "Applicant " + file.getOriginalFilename());
        candidate.setEmail(email);
        candidate.setPhone(parsed.getExtractedPhone());
        candidate.setCurrentTitle(parsed.getExtractedTitle());
        candidate.setYearsExperience(parsed.getEstimatedExperienceYears());
        candidate.setHighestEducation(parsed.getExtractedEducation());
        candidate.setSkillsSummary(String.join(", ", parsed.getExtractedSkills()));
        candidate.setResumeText(parsed.getRawText());
        candidate.setBio("Auto-parsed from uploaded resume: " + file.getOriginalFilename());

        return candidateRepository.save(candidate);
    }

    public Candidate processResumeText(String resumeText, String candidateName, String email) {
        ResumeParseResult parsed = resumeParserService.parseResumeText(resumeText);

        Candidate candidate = new Candidate();
        candidate.setFullName(candidateName != null && !candidateName.isBlank() ? candidateName : (parsed.getExtractedName() != null ? parsed.getExtractedName() : "Anonymous Candidate"));
        candidate.setEmail(email != null && !email.isBlank() ? email : (parsed.getExtractedEmail() != null ? parsed.getExtractedEmail() : "candidate-" + System.currentTimeMillis() + "@talent.io"));
        candidate.setPhone(parsed.getExtractedPhone());
        candidate.setCurrentTitle(parsed.getExtractedTitle());
        candidate.setYearsExperience(parsed.getEstimatedExperienceYears());
        candidate.setHighestEducation(parsed.getExtractedEducation());
        candidate.setSkillsSummary(String.join(", ", parsed.getExtractedSkills()));
        candidate.setResumeText(resumeText);
        candidate.setBio("Created via AI text scanner.");

        return candidateRepository.save(candidate);
    }

    @Transactional(readOnly = true)
    public List<Candidate> searchCandidates(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCandidates();
        }
        return candidateRepository.searchCandidates(query.trim());
    }
}
