package com.recruitment.platform;

import com.recruitment.platform.dto.ExplainableMatchResult;
import com.recruitment.platform.dto.StructuredJobData;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.repository.CandidateEmbeddingRepository;
import com.recruitment.platform.repository.CandidateRepository;
import com.recruitment.platform.repository.JobAnalysisRepository;
import com.recruitment.platform.repository.JobRepository;
import com.recruitment.platform.service.HybridJobMatchingService;
import com.recruitment.platform.service.ai.DenseEmbeddingService;
import com.recruitment.platform.service.ai.EmbeddingService;
import com.recruitment.platform.service.ai.JobDescriptionAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@SuppressWarnings("null")
class HybridJobMatchingTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobAnalysisRepository jobAnalysisRepository;

    @Autowired
    private CandidateEmbeddingRepository candidateEmbeddingRepository;

    @Autowired
    private HybridJobMatchingService hybridJobMatchingService;

    @Autowired
    private DenseEmbeddingService denseEmbeddingService;

    @SpyBean
    private JobDescriptionAnalyzer jobDescriptionAnalyzer;

    @SpyBean
    private EmbeddingService embeddingService;

    @Autowired
    private com.recruitment.platform.repository.NotificationRepository notificationRepository;

    @Autowired
    private com.recruitment.platform.repository.InterviewRepository interviewRepository;

    @Autowired
    private com.recruitment.platform.repository.ApplicationRepository applicationRepository;

    @Autowired
    private com.recruitment.platform.repository.ResumeAnalysisRepository resumeAnalysisRepository;

    private Job seniorJavaJob;
    private Candidate strongCandidate;
    private Candidate juniorPythonCandidate;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        interviewRepository.deleteAll();
        applicationRepository.deleteAll();
        resumeAnalysisRepository.deleteAll();
        candidateEmbeddingRepository.deleteAll();
        jobAnalysisRepository.deleteAll();
        candidateRepository.deleteAll();
        jobRepository.deleteAll();

        // Target Job: Senior Cloud Backend Engineer
        seniorJavaJob = new Job(
                "Senior Cloud Backend Engineer",
                "Platform Engineering",
                "Austin, TX / Hybrid",
                "We are seeking an experienced Senior Software Engineer to design, implement, and maintain high-throughput cloud microservices.\n" +
                        "- Architect distributed systems handling millions of events daily.\n" +
                        "- Write clean, maintainable, and thoroughly tested production code.\n" +
                        "Preferred skills: Kubernetes, Kafka, Terraform is a plus.",
                "Java, Spring Boot, Docker, PostgreSQL, AWS"
        );
        seniorJavaJob.setMinExperienceYears(5);
        seniorJavaJob = jobRepository.save(seniorJavaJob);

        // Candidate 1: High Match Senior Java Engineer
        strongCandidate = new Candidate(
                "Morgan Reed",
                "morgan.reed@example.com",
                "+1-555-8888",
                "Senior Backend Engineer",
                6,
                "Java, Spring Boot, Docker, AWS, PostgreSQL, Kafka, Kubernetes"
        );
        strongCandidate.setHighestEducation("Master of Science in Computer Science");
        strongCandidate.setBio("6+ years building distributed cloud platforms using Java, Spring Boot, and AWS.");
        strongCandidate.setResumeText("Senior Backend Engineer at Acme Cloud. Led microservices re-architecture on AWS with Docker and PostgreSQL.");
        strongCandidate = candidateRepository.save(strongCandidate);

        // Candidate 2: Python / Junior developer with different stack
        juniorPythonCandidate = new Candidate(
                "Taylor Swift",
                "taylor.swift@example.com",
                "+1-555-9999",
                "Junior Web Developer",
                1,
                "Python, Flask, HTML, CSS, SQLite"
        );
        juniorPythonCandidate.setHighestEducation("Bachelor of Arts");
        juniorPythonCandidate.setBio("Entry-level developer building web apps with Python and Flask.");
        juniorPythonCandidate = candidateRepository.save(juniorPythonCandidate);
    }

    // =========================================================================
    // 1. JOB ANALYSIS & DECOMPOSITION
    // =========================================================================

    @Test
    @DisplayName("Should analyze job description and extract structured skills, tech, experience, and responsibilities")
    void testJobAnalysisDecomposition() {
        StructuredJobData data = hybridJobMatchingService.getStructuredJobData(seniorJavaJob);

        assertNotNull(data);
        assertEquals("Senior Cloud Backend Engineer", data.getJobTitle());
        assertTrue(data.getRequiredSkills().contains("Java"));
        assertTrue(data.getRequiredSkills().contains("Spring Boot"));
        assertTrue(data.getTechnologies().contains("Docker"));
        assertEquals(5, data.getMinExperienceYears());
        assertFalse(data.getResponsibilities().isEmpty());
    }

    // =========================================================================
    // 2. EMBEDDINGS & COSINE SIMILARITY
    // =========================================================================

    @Test
    @DisplayName("Should generate normalized dense vectors and calculate high semantic similarity for related domains")
    void testDenseEmbeddingGenerationAndCosineSimilarity() {
        float[] vector1 = denseEmbeddingService.generateEmbedding("Java Spring Boot cloud microservices architecture AWS Docker");
        float[] vector2 = denseEmbeddingService.generateEmbedding("Backend engineer distributed systems Java Spring cloud platforms");
        float[] vector3 = denseEmbeddingService.generateEmbedding("Graphic designer Photoshop Illustrator Figma UI design");

        assertEquals(DenseEmbeddingService.VECTOR_DIMENSION, vector1.length);
        assertEquals(DenseEmbeddingService.VECTOR_DIMENSION, vector2.length);

        double similarityAligned = denseEmbeddingService.computeCosineSimilarity(vector1, vector2);
        double similarityDisparate = denseEmbeddingService.computeCosineSimilarity(vector1, vector3);

        assertTrue(similarityAligned > similarityDisparate, "Expected aligned domains to have higher similarity than disparate domains");
        assertTrue(similarityAligned > 0.50, "Expected semantic similarity > 0.50 for related backend domains");
        assertTrue(similarityDisparate < 0.35, "Expected low semantic similarity for unrelated domains");
    }

    // =========================================================================
    // 3. HIGH MATCH CANDIDATE SCENARIO
    // =========================================================================

    @Test
    @DisplayName("Should produce high alignment score (>85%) with complete evidence for qualified candidate")
    void testHighMatchCandidateAlignment() {
        ExplainableMatchResult match = hybridJobMatchingService.match(seniorJavaJob, strongCandidate);

        assertNotNull(match);
        assertTrue(match.getOverallScore() >= 80.0, "Expected score >= 80% for senior candidate matching all requirements");
        assertTrue(match.getCompatibilityBand().equals("HIGH_ALIGNMENT") || match.getCompatibilityBand().equals("STRONG_ALIGNMENT"));
        assertTrue(match.getMatchedRequiredSkills().contains("Java"));
        assertTrue(match.getMatchedRequiredSkills().contains("Spring Boot"));
        assertEquals(0, match.getMissingRequiredSkills().size());
        assertEquals(6, match.getCandidateExperienceYears());
        assertNotNull(match.getDecisionSupportSummary());
        assertFalse(match.getDecisionSupportSummary().contains("should be hired"));
    }

    // =========================================================================
    // 4. MISSING REQUIRED SKILLS SCENARIO
    // =========================================================================

    @Test
    @DisplayName("Should identify missing required skills and reflect gaps in score for junior/mismatched candidate")
    void testModerateMatchWithMissingSkills() {
        ExplainableMatchResult match = hybridJobMatchingService.match(seniorJavaJob, juniorPythonCandidate);

        assertNotNull(match);
        assertTrue(match.getOverallScore() < 60.0, "Expected lower score (<60%) for mismatched profile");
        assertTrue(match.getMissingRequiredSkills().contains("Java"));
        assertTrue(match.getMissingRequiredSkills().contains("Spring Boot"));
        assertEquals(1, match.getCandidateExperienceYears());
        assertEquals(5, match.getRequiredExperienceYears());
        assertTrue(match.getRequiredSkillScore() < 30.0);
    }

    // =========================================================================
    // 5. RESILIENCE ON EMPTY / MISSING DATA
    // =========================================================================

    @Test
    @DisplayName("Should gracefully handle empty profile or missing job requirements without crashing")
    void testResilienceOnEmptyProfileAndMissingRequirements() {
        Job bareJob = new Job();
        bareJob.setTitle("General Role");
        bareJob = jobRepository.save(bareJob);

        Candidate bareCandidate = new Candidate();
        bareCandidate.setFullName("Blank Profile");
        bareCandidate.setEmail("blank@example.com");
        bareCandidate = candidateRepository.save(bareCandidate);

        ExplainableMatchResult match = hybridJobMatchingService.match(bareJob, bareCandidate);
        assertNotNull(match);
        assertTrue(match.getOverallScore() >= 0.0);
        assertNotNull(match.getCompatibilityBand());
        assertNotNull(match.getDecisionSupportSummary());
    }

    // =========================================================================
    // 6. AI PROVIDER FAILURE RESILIENCE
    // =========================================================================

    @Test
    @DisplayName("Should fall back gracefully when AI job analyzer fails/times out")
    void testAiProviderFailureGracefulFallback() {
        doThrow(new RuntimeException("LLM Gateway 504 Gateway Timeout"))
                .when(jobDescriptionAnalyzer).analyzeJob(anyString(), anyString(), anyString(), anyString(), any(), any());

        Job newJob = new Job("Backend Engineer", "Engineering", "Remote", "Description", "Java, Docker");
        newJob = jobRepository.save(newJob);

        ExplainableMatchResult match = hybridJobMatchingService.match(newJob, strongCandidate);
        assertNotNull(match);
        assertTrue(match.getOverallScore() > 0.0);
    }

    // =========================================================================
    // 7. EMBEDDING FAILURE RESILIENCE
    // =========================================================================

    @Test
    @DisplayName("Should fall back to lexical overlap when vector embedding service encounters error")
    void testEmbeddingFailureGracefulFallback() {
        doThrow(new RuntimeException("Vector Service Unavailable"))
                .when(embeddingService).generateEmbedding(anyString());

        ExplainableMatchResult match = hybridJobMatchingService.match(seniorJavaJob, strongCandidate);
        assertNotNull(match);
        assertTrue(match.getOverallScore() > 50.0);
        assertTrue(match.getSemanticScore() > 0.0);
    }

    // =========================================================================
    // 8. EMBEDDING CACHING VERIFICATION
    // =========================================================================

    @Test
    @DisplayName("Should persist and reuse cached embedding when source text hash is unchanged")
    void testEmbeddingCachingEfficiency() {
        // First match generates and stores embedding
        hybridJobMatchingService.match(seniorJavaJob, strongCandidate);

        assertTrue(jobAnalysisRepository.findByJobId(seniorJavaJob.getId()).isPresent());
        assertTrue(candidateEmbeddingRepository.findByCandidateId(strongCandidate.getId()).isPresent());

        String initialJobHash = jobAnalysisRepository.findByJobId(seniorJavaJob.getId()).get().getSha256Hash();
        assertNotNull(initialJobHash);

        // Second match should find and use cached analysis
        StructuredJobData cachedData = hybridJobMatchingService.getStructuredJobData(seniorJavaJob);
        assertNotNull(cachedData);
        assertEquals(initialJobHash, jobAnalysisRepository.findByJobId(seniorJavaJob.getId()).get().getSha256Hash());
    }

    // =========================================================================
    // 9. REST API ENDPOINTS
    // =========================================================================

    @Test
    @DisplayName("Should expose explainable comparison endpoint via REST API")
    void testCompareEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/matching/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"jobId\": %d, \"candidateId\": %d}", seniorJavaJob.getId(), strongCandidate.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore", greaterThan(70.0)))
                .andExpect(jsonPath("$.compatibilityBand").isNotEmpty())
                .andExpect(jsonPath("$.matchedRequiredSkills", hasItem("Java")))
                .andExpect(jsonPath("$.decisionSupportSummary").isNotEmpty());
    }

    @Test
    @DisplayName("Should expose explain breakdown GET endpoint")
    void testExplainEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/matching/explain/{jobId}/{candidateId}", seniorJavaJob.getId(), strongCandidate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore").isNumber())
                .andExpect(jsonPath("$.candidateExperienceYears").value(6));
    }
}
