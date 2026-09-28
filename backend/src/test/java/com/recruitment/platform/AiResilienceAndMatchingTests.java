package com.recruitment.platform;

import com.recruitment.platform.dto.ExplainableMatchResult;
import com.recruitment.platform.dto.InterviewPrepDto;
import com.recruitment.platform.dto.StructuredJobData;
import com.recruitment.platform.model.*;
import com.recruitment.platform.repository.*;
import com.recruitment.platform.service.HybridJobMatchingService;
import com.recruitment.platform.service.ai.AiInterviewAssistantService;
import com.recruitment.platform.service.ai.AiJobDescriptionAnalyzer;
import com.recruitment.platform.service.ai.DenseEmbeddingService;
import com.recruitment.platform.service.observability.ObservabilityMetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@SuppressWarnings("null")
class AiResilienceAndMatchingTests {

    @Autowired
    private AiJobDescriptionAnalyzer jobAnalyzer;

    @Autowired
    private DenseEmbeddingService embeddingService;

    @Autowired
    private HybridJobMatchingService matchingService;

    @Autowired
    private AiInterviewAssistantService interviewAssistantService;

    @Autowired
    private ObservabilityMetricsService observabilityMetricsService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JobAnalysisRepository jobAnalysisRepository;

    @Autowired
    private CandidateEmbeddingRepository candidateEmbeddingRepository;

    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        notificationRepository.deleteAll();
        interviewRepository.deleteAll();
        applicationRepository.deleteAll();
        resumeAnalysisRepository.deleteAll();
        candidateEmbeddingRepository.deleteAll();
        candidateRepository.deleteAll();
        jobAnalysisRepository.deleteAll();
        jobRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    // =========================================================================
    // 1. Structured Output Validation Tests
    // =========================================================================

    @Test
    @DisplayName("AI Job Analyzer extracts required skills, preferred skills, experience, and role summary")
    void testStructuredJobAnalysisOutput() {
        String title = "Principal Distributed Systems Engineer";
        String description = "Lead architectural design of high-throughput event-driven microservices. Minimum 8+ years of relevant experience required.";
        String requirements = "Proficiency in Java, Spring Boot, Kubernetes, Docker, and Kafka. M.S. in Computer Science preferred.";
        String rawSkills = "Java, Spring Boot, Microservices, Kafka";

        StructuredJobData result = jobAnalyzer.analyzeJob(title, description, requirements, rawSkills, 8, "Master");

        assertNotNull(result);
        assertEquals("Principal Distributed Systems Engineer", result.getJobTitle());
        assertNotNull(result.getRoleSummary());
        assertFalse(result.getRoleSummary().isBlank());

        // Verify skill taxonomy extraction
        assertTrue(result.getRequiredSkills().contains("Java") || result.getRequiredSkills().contains("Spring Boot"));
        assertNotNull(result.getTechnologies());
        assertTrue(result.getTechnologies().size() >= 2);

        // Experience extraction
        assertNotNull(result.getMinExperienceYears());
        assertTrue(result.getMinExperienceYears() >= 5);
    }

    @Test
    @DisplayName("AI Job Analyzer handles null and empty inputs safely without crashing")
    void testStructuredJobAnalysisFallbackOnEmptyInputs() {
        StructuredJobData result = jobAnalyzer.analyzeJob(null, null, null, null, null, null);

        assertNotNull(result);
        assertNotNull(result.getJobTitle());
        assertNotNull(result.getRoleSummary());
        assertNotNull(result.getRequiredSkills());
        assertNotNull(result.getPreferredSkills());
        assertNotNull(result.getTechnologies());
    }

    // =========================================================================
    // 2. Dense Vector & Embedding Mathematical Tests
    // =========================================================================

    @Test
    @DisplayName("Dense embeddings produce fixed 128-dimensional vectors with unit L2 norm")
    void testEmbeddingDimensionsAndNormalization() {
        String sampleText = "Senior Cloud Architect specializing in AWS, Kubernetes, Terraform, and Go.";

        float[] embedding = embeddingService.generateEmbedding(sampleText);

        assertNotNull(embedding);
        assertEquals(DenseEmbeddingService.VECTOR_DIMENSION, embedding.length);

        // Calculate L2 norm: sqrt(sum(x_i^2))
        double sumSq = 0.0;
        for (float v : embedding) {
            sumSq += v * v;
        }
        double norm = Math.sqrt(sumSq);
        assertEquals(1.0, norm, 1e-4, "L2 norm must equal 1.0 for normalized unit sphere embeddings");
    }

    @Test
    @DisplayName("Cosine similarity satisfies reflexivity, symmetry, and Cauchy-Schwarz bounds")
    void testCosineSimilarityMathProperties() {
        String textA = "Java Spring Boot microservices backend engineer with PostgreSQL experience.";
        String textB = "Backend developer working on Java Spring applications and SQL databases.";
        String textC = "Pastry chef baking artisanal sourdough bread and French pastries.";

        float[] vecA = embeddingService.generateEmbedding(textA);
        float[] vecB = embeddingService.generateEmbedding(textB);
        float[] vecC = embeddingService.generateEmbedding(textC);

        // Reflexivity: sim(A, A) == 1.0
        double simAA = embeddingService.computeCosineSimilarity(vecA, vecA);
        assertEquals(1.0, simAA, 1e-4, "Self-similarity must equal 1.0");

        // Symmetry: sim(A, B) == sim(B, A)
        double simAB = embeddingService.computeCosineSimilarity(vecA, vecB);
        double simBA = embeddingService.computeCosineSimilarity(vecB, vecA);
        assertEquals(simAB, simBA, 1e-6, "Cosine similarity must be symmetric");

        // Semantic discrimination: sim(backend, backend) > sim(backend, pastry chef)
        double simAC = embeddingService.computeCosineSimilarity(vecA, vecC);
        assertTrue(simAB > simAC, "Related technical texts must have higher semantic similarity than unrelated domains");

        // Mathematical bounds [-1.0, 1.0]
        assertTrue(simAB >= -1.0 && simAB <= 1.0);
        assertTrue(simAC >= -1.0 && simAC <= 1.0);
    }

    @Test
    @DisplayName("Zero vector or null vector inputs return 0.0 similarity without NaN or throwing")
    void testZeroVectorHandling() {
        float[] emptyVec = new float[DenseEmbeddingService.VECTOR_DIMENSION];
        float[] normalVec = embeddingService.generateEmbedding("Testing zero vector fallback");

        double sim = embeddingService.computeCosineSimilarity(emptyVec, normalVec);
        assertFalse(Double.isNaN(sim), "Similarity with zero vector must not produce NaN");
        assertEquals(0.0, sim, 1e-6);

        double nullSim = embeddingService.computeCosineSimilarity(null, normalVec);
        assertEquals(0.0, nullSim, 1e-6);
    }

    // =========================================================================
    // 3. Deterministic Hybrid Matching Business Rules
    // =========================================================================

    @Test
    @DisplayName("Hybrid matching produces explainable breakdown with weighted sub-scores")
    void testHybridMatchingDeterministicScoring() {
        Job job = new Job();
        job.setTitle("Senior Java Backend Engineer");
        job.setSkills("Java, Spring Boot, PostgreSQL, Docker, Microservices");
        job.setDescription("Build enterprise microservices using Java and Spring Boot.");
        job.setRequirements("5+ years experience, B.S. in Computer Science.");
        job.setMinExperienceYears(5);
        job = jobRepository.save(job);

        Candidate candidate = new Candidate();
        candidate.setFullName("Alice Developer");
        candidate.setEmail("alice.dev@example.com");
        candidate.setCurrentTitle("Senior Java Developer");
        candidate.setYearsExperience(6);
        candidate.setHighestEducation("B.S. in Computer Science");
        candidate.setSkillsSummary("Java, Spring Boot, PostgreSQL, Docker, Microservices, Git");
        candidate = candidateRepository.save(candidate);

        ExplainableMatchResult match = matchingService.match(job, candidate);

        assertNotNull(match);
        assertTrue(match.getOverallScore() >= 80.0, "Qualified candidate should receive high match score");
        assertNotNull(match.getCompatibilityBand());

        // Verify sub-score components are present and bounded
        assertTrue(match.getRequiredSkillScore() >= 70.0);
        assertTrue(match.getExperienceScore() >= 90.0);
        assertTrue(match.getEducationScore() >= 80.0);

        // Verify evidence
        assertNotNull(match.getMatchedRequiredSkills());
        assertFalse(match.getMatchedRequiredSkills().isEmpty());
    }

    @Test
    @DisplayName("Candidate with no overlapping skills receives low score but clean explainability")
    void testHybridMatchingZeroOverlapCandidate() {
        Job job = new Job();
        job.setTitle("DevOps Infrastructure Architect");
        job.setSkills("Kubernetes, Terraform, AWS, Linux, Prometheus");
        job.setDescription("Manage large Kubernetes clusters and Terraform infrastructure.");
        job.setMinExperienceYears(7);
        job = jobRepository.save(job);

        Candidate candidate = new Candidate();
        candidate.setFullName("Graphic Designer");
        candidate.setEmail("designer@example.com");
        candidate.setCurrentTitle("Graphic Designer");
        candidate.setYearsExperience(1);
        candidate.setHighestEducation("High School");
        candidate.setSkillsSummary("Photoshop, Illustrator, Figma, Typography");
        candidate = candidateRepository.save(candidate);

        ExplainableMatchResult match = matchingService.match(job, candidate);

        assertNotNull(match);
        assertTrue(match.getOverallScore() < 50.0, "Unqualified candidate must receive low match score");
        assertTrue(match.getMatchedRequiredSkills().isEmpty());
        assertFalse(match.getMissingRequiredSkills().isEmpty());
    }

    // =========================================================================
    // 4. AI Interview Preparation Guide Validation
    // =========================================================================

    @Test
    @DisplayName("AI Interview Assistant generates structured prep guide with categorized questions")
    void testInterviewAssistantStructuredGuide() {
        Job job = new Job();
        job.setTitle("Full Stack Engineer");
        job.setSkills("React, TypeScript, Java, Spring Boot, PostgreSQL");
        job.setRequirements("3+ years experience with modern React and Java backends.");
        job.setDescription("Full stack engineering role building customer-facing web apps.");
        job = jobRepository.save(job);

        Candidate candidate = new Candidate();
        candidate.setFullName("Bob Candidate");
        candidate.setEmail("bob.candidate@example.com");
        candidate.setCurrentTitle("Junior Software Developer");
        candidate.setYearsExperience(2);
        candidate.setSkillsSummary("Java, Spring Boot, SQL");
        candidate = candidateRepository.save(candidate);

        InterviewPrepDto guide = interviewAssistantService.generateInterviewPrep(job.getId(), candidate.getId());

        assertNotNull(guide);
        assertEquals(candidate.getId(), guide.getCandidateId());
        assertEquals(job.getId(), guide.getJobId());

        // Verify structured sections
        assertNotNull(guide.getPreparationTopics());
        assertFalse(guide.getPreparationTopics().isEmpty());

        assertNotNull(guide.getTechnicalQuestions());
        assertFalse(guide.getTechnicalQuestions().isEmpty());

        assertNotNull(guide.getHrQuestions());
        assertFalse(guide.getHrQuestions().isEmpty());

        assertNotNull(guide.getMatchedSkills());
        assertNotNull(guide.getMissingOrGrowthSkills());
        // Candidate has Java & Spring Boot matched, but React & TypeScript missing
        assertTrue(guide.getMatchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("Java")));
        assertTrue(guide.getMissingOrGrowthSkills().stream().anyMatch(s -> s.equalsIgnoreCase("React")));
    }

    // =========================================================================
    // 5. Observability Metrics Instrumentation
    // =========================================================================

    @Test
    @DisplayName("Observability metrics service accurately records AI latency and failure counters")
    void testObservabilityMetricsRecording() throws Exception {
        MeterRegistry registry = observabilityMetricsService.getMeterRegistry();

        // 1. Record successful AI operation
        String result = observabilityMetricsService.recordAiOperation("job_analysis", () -> "SUCCESS_PAYLOAD");
        assertEquals("SUCCESS_PAYLOAD", result);

        Counter requests = registry.find("recruitment.ai.requests.total")
                .tag("operation", "job_analysis")
                .counter();
        assertNotNull(requests);
        assertTrue(requests.count() >= 1.0);

        Timer timer = registry.find("recruitment.ai.latency")
                .tag("operation", "job_analysis")
                .timer();
        assertNotNull(timer);
        assertTrue(timer.count() >= 1);

        // 2. Record failed AI operation
        assertThrows(IllegalStateException.class, () -> {
            observabilityMetricsService.recordAiOperation("embedding_generation", (Callable<String>) () -> {
                throw new IllegalStateException("OpenAI rate limit exceeded");
            });
        });

        Counter failures = registry.find("recruitment.ai.failures.total")
                .tag("operation", "embedding_generation")
                .counter();
        assertNotNull(failures);
        assertTrue(failures.count() >= 1.0);
    }
}
