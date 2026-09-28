package com.recruitment.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.platform.dto.*;
import com.recruitment.platform.model.*;
import com.recruitment.platform.repository.*;
import com.recruitment.platform.service.CandidateSearchService;
import com.recruitment.platform.service.JobSearchService;
import com.recruitment.platform.service.RecommendationService;
import com.recruitment.platform.service.SkillGapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@SuppressWarnings("null")
class SearchAndRecommendationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateEmbeddingRepository candidateEmbeddingRepository;

    @Autowired
    private JobAnalysisRepository jobAnalysisRepository;

    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobSearchService jobSearchService;

    @Autowired
    private CandidateSearchService candidateSearchService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private SkillGapService skillGapService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    private User recruiterUser;
    private User candidateUser;
    private Candidate testCandidate;
    private Job backendJob;
    private Job frontendJob;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        interviewRepository.deleteAll();
        jobAnalysisRepository.deleteAll();
        candidateEmbeddingRepository.deleteAll();
        resumeAnalysisRepository.deleteAll();
        applicationRepository.deleteAll();
        candidateRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Seed Users
        recruiterUser = new User("recruiter.jane@example.com", "Recruiter Jane", "passwordHash", Role.RECRUITER);
        userRepository.save(recruiterUser);

        candidateUser = new User("john.dev@example.com", "John Dev", "passwordHash", Role.CANDIDATE);
        userRepository.save(candidateUser);

        testCandidate = new Candidate();
        testCandidate.setFullName("John Dev");
        testCandidate.setEmail("john.dev@example.com");
        testCandidate.setCurrentTitle("Senior Java Engineer");
        testCandidate.setLocation("Remote");
        testCandidate.setYearsExperience(6);
        testCandidate.setHighestEducation("Master of Science in Computer Science");
        testCandidate.setSkillsSummary("Java, Spring Boot, Microservices, PostgreSQL, Docker, Git");
        testCandidate.setResumeText("6 years developing enterprise Spring Boot microservices, REST APIs, and PostgreSQL datastores.");
        testCandidate = candidateRepository.save(testCandidate);

        // 3. Seed Jobs
        backendJob = new Job(
                "Lead Java Cloud Architect",
                "Engineering",
                "Remote",
                "Architecting high-scale Java Spring Boot distributed cloud systems with Kafka and Docker.",
                "Java, Spring Boot, Microservices, Kafka, Docker, Kubernetes"
        );
        backendJob.setJobType("FULL_TIME");
        backendJob.setExperienceLevel("LEAD");
        backendJob.setMinExperienceYears(5);
        backendJob.setStatus("ACTIVE");
        backendJob = jobRepository.save(backendJob);

        frontendJob = new Job(
                "Senior Frontend React Developer",
                "Engineering",
                "New York",
                "Building dynamic modern user interfaces with React, TypeScript, and CSS3.",
                "React, TypeScript, Next.js, Redux, CSS"
        );
        frontendJob.setJobType("CONTRACT");
        frontendJob.setExperienceLevel("SENIOR");
        frontendJob.setMinExperienceYears(4);
        frontendJob.setStatus("ACTIVE");
        frontendJob = jobRepository.save(frontendJob);
    }

    @Test
    @DisplayName("Job Search: Multi-criteria filtering by department, location, jobType, and experience level")
    void testJobSearch_FiltersAndPagination() {
        JobSearchCriteriaDto criteria = new JobSearchCriteriaDto();
        criteria.setDepartment("Engineering");
        criteria.setLocation("Remote");
        criteria.setJobType("FULL_TIME");
        criteria.setMinExperienceYears(4);

        Page<JobSearchResultDto> results = jobSearchService.searchJobs(
                criteria,
                PageRequest.of(0, 10, Sort.by("createdAt").descending())
        );

        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
        assertEquals("Lead Java Cloud Architect", results.getContent().get(0).getJob().getTitle());
        assertTrue(results.getContent().get(0).getMatchHighlights().contains("Department: Engineering"));
    }

    @Test
    @DisplayName("Job Search: Text keyword search across title, description, and skills")
    void testJobSearch_KeywordMatchAcrossFields() {
        JobSearchCriteriaDto criteria = new JobSearchCriteriaDto();
        criteria.setQuery("React");

        Page<JobSearchResultDto> results = jobSearchService.searchJobs(criteria, PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("Senior Frontend React Developer", results.getContent().get(0).getJob().getTitle());
    }

    @Test
    @DisplayName("Job Search: Semantic reranking using dense query vector cosine similarity")
    void testJobSearch_SemanticReranking() {
        JobSearchCriteriaDto criteria = new JobSearchCriteriaDto();
        criteria.setSemanticQuery("Distributed enterprise backend services with relational databases");

        Page<JobSearchResultDto> results = jobSearchService.searchJobs(criteria, PageRequest.of(0, 10));

        assertEquals(2, results.getTotalElements());
        // Backend Java job should be ranked #1 with higher semantic score than React frontend job
        JobSearchResultDto topResult = results.getContent().get(0);
        assertEquals("Lead Java Cloud Architect", topResult.getJob().getTitle());
        assertNotNull(topResult.getSemanticRelevanceScore());
        assertTrue(topResult.getSemanticRelevanceScore() > 0.0);
    }

    @Test
    @DisplayName("Job Search: Empty results handled cleanly when no jobs match criteria")
    void testJobSearch_EmptyResults() {
        JobSearchCriteriaDto criteria = new JobSearchCriteriaDto();
        criteria.setDepartment("Human Resources");

        Page<JobSearchResultDto> results = jobSearchService.searchJobs(criteria, PageRequest.of(0, 10));

        assertNotNull(results);
        assertEquals(0, results.getTotalElements());
        assertTrue(results.getContent().isEmpty());
    }

    @Test
    @DisplayName("Candidate Search: Recruiter access permitted with skills and experience filtering")
    void testCandidateSearch_AuthorizedRecruiterAccess() {
        CandidateSearchCriteriaDto criteria = new CandidateSearchCriteriaDto();
        criteria.setSkills(List.of("Java"));
        criteria.setMinExperienceYears(5.0);
        criteria.setLocation("Remote");

        Page<CandidateSummaryDto> results = candidateSearchService.searchCandidates(
                criteria,
                PageRequest.of(0, 10),
                "recruiter.jane@example.com"
        );

        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
        assertEquals("John Dev", results.getContent().get(0).getFullName());
    }

    @Test
    @DisplayName("Candidate Search: Unauthorized candidate role rejected with AccessDeniedException")
    void testCandidateSearch_UnauthorizedAccess_Forbidden() {
        CandidateSearchCriteriaDto criteria = new CandidateSearchCriteriaDto();
        criteria.setQuery("Java");

        assertThrows(AccessDeniedException.class, () ->
                candidateSearchService.searchCandidates(criteria, PageRequest.of(0, 10), "john.dev@example.com")
        );
    }

    @Test
    @DisplayName("Job Recommendations: Generates ranked jobs for candidate with preference signals and disclaimer")
    void testJobRecommendations_ForCandidate() {
        List<JobRecommendationDto> recs = recommendationService.getJobRecommendations(
                testCandidate.getId(),
                5,
                "john.dev@example.com"
        );

        assertNotNull(recs);
        assertFalse(recs.isEmpty());

        JobRecommendationDto topRec = recs.get(0);
        assertEquals("Lead Java Cloud Architect", topRec.getJob().getTitle());
        assertTrue(topRec.getMatchScore() >= 60.0);
        assertNotNull(topRec.getMatchLevel());
        assertFalse(topRec.getMatchingFactors().isEmpty());
        // Verify responsible AI disclaimer
        assertNotNull(topRec.getDisclaimer());
        assertTrue(topRec.getDisclaimer().contains("not a guarantee of interview or employment"));
    }

    @Test
    @DisplayName("Candidate Recommendations: Recommends candidates for open requisition to authorized recruiter")
    void testCandidateRecommendations_ForJob_Authorized() {
        List<CandidateRecommendationDto> recs = recommendationService.getCandidateRecommendations(
                backendJob.getId(),
                5,
                "recruiter.jane@example.com"
        );

        assertNotNull(recs);
        assertFalse(recs.isEmpty());

        CandidateRecommendationDto candidateRec = recs.get(0);
        assertEquals("John Dev", candidateRec.getCandidate().getFullName());
        assertTrue(candidateRec.getMatchScore() >= 60.0);
        assertNotNull(candidateRec.getMatchedRequiredSkills());
    }

    @Test
    @DisplayName("Skill Gap Analysis: Identifies present, less-evident, and missing skills with learning plan")
    void testSkillGapAnalysis_PresentAndMissingSkills() {
        SkillGapAnalysisDto gapAnalysis = skillGapService.analyzeSkillGap(backendJob.getId(), testCandidate.getId());

        assertNotNull(gapAnalysis);
        assertEquals("Lead Java Cloud Architect", gapAnalysis.getJobTitle());
        assertEquals("John Dev", gapAnalysis.getCandidateName());

        // Present skills
        assertFalse(gapAnalysis.getPresentSkills().isEmpty());
        assertTrue(gapAnalysis.getPresentSkills().stream().anyMatch(s -> s.equalsIgnoreCase("Java")));

        // Missing skills & critical gaps
        assertNotNull(gapAnalysis.getMissingRequiredSkills());
        assertNotNull(gapAnalysis.getCriticalGaps());

        // Actionable learning plan
        assertNotNull(gapAnalysis.getActionableLearningPlan());
        assertFalse(gapAnalysis.getActionableLearningPlan().isEmpty());

        // Ethical notice
        assertNotNull(gapAnalysis.getEthicalNotice());
        assertTrue(gapAnalysis.getEthicalNotice().contains("Protected characteristics are never used"));
    }

    @Test
    @DisplayName("REST API: Endpoints for search, recommendations, and skill-gap return HTTP 200")
    @WithMockUser(username = "recruiter.jane@example.com", roles = {"RECRUITER"})
    void testRestEndpoints_SearchAndRecommendations() throws Exception {
        // 1. Job search POST /api/v1/jobs/search
        mockMvc.perform(post("/api/v1/jobs/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JobSearchCriteriaDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        // 2. Candidate recommendations GET /api/v1/recommendations/candidates/{jobId}
        mockMvc.perform(get("/api/v1/recommendations/candidates/" + backendJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // 3. Skill-gap GET /api/v1/skill-gap/job/{jobId}/candidate/{candidateId}
        mockMvc.perform(get("/api/v1/skill-gap/job/" + backendJob.getId() + "/candidate/" + testCandidate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readinessScore", notNullValue()))
                .andExpect(jsonPath("$.actionableLearningPlan", notNullValue()));
    }
}
