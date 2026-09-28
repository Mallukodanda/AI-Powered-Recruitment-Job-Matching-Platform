package com.recruitment.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.platform.dto.StructuredResumeData;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.ResumeAnalysis;
import com.recruitment.platform.model.ResumeAnalysisStatus;
import com.recruitment.platform.repository.CandidateRepository;
import com.recruitment.platform.repository.ResumeAnalysisRepository;
import com.recruitment.platform.service.ai.StructuredResumeAnalyzer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@SuppressWarnings("null")
class ResumeProcessingTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @Autowired
    private com.recruitment.platform.repository.CandidateEmbeddingRepository candidateEmbeddingRepository;

    @SpyBean
    private StructuredResumeAnalyzer structuredResumeAnalyzer;

    @Autowired
    private com.recruitment.platform.repository.NotificationRepository notificationRepository;

    @Autowired
    private com.recruitment.platform.repository.InterviewRepository interviewRepository;

    @Autowired
    private com.recruitment.platform.repository.ApplicationRepository applicationRepository;

    private Candidate testCandidate;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        interviewRepository.deleteAll();
        applicationRepository.deleteAll();
        candidateEmbeddingRepository.deleteAll();
        resumeAnalysisRepository.deleteAll();
        candidateRepository.deleteAll();

        testCandidate = new Candidate(
                "Alex Rivera",
                "alex.rivera@example.com",
                "+1-555-4321",
                "Software Engineer",
                3,
                "Java, Spring"
        );
        testCandidate.setLocation("Seattle, WA");
        testCandidate = candidateRepository.save(testCandidate);
    }

    // =========================================================================
    // 1. VALID PDF UPLOAD & PARSING
    // =========================================================================

    @Test
    @DisplayName("Should upload valid PDF, validate magic bytes, extract text, and generate structured analysis")
    void testUploadValidPdf() throws Exception {
        byte[] pdfBytes = createTestPdfBytes("Alex Rivera\nalex.rivera@example.com | +1 555-4321\nSenior Software Engineer\nSkills: Java, Spring Boot, Docker, Kubernetes, AWS\nExperience: Senior Backend Engineer at Acme Corp (2020 - Present)");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "alex_rivera_resume.pdf",
                "application/pdf",
                pdfBytes
        );

        MvcResult result = mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.originalFilename").value("alex_rivera_resume.pdf"))
                .andExpect(jsonPath("$.status").value("ANALYZED"))
                .andExpect(jsonPath("$.aiProvider").value("ENTERPRISE_STRUCTURAL_AI"))
                .andExpect(jsonPath("$.structuredData.name").value("Alex Rivera"))
                .andExpect(jsonPath("$.structuredData.skills", hasItems("Java", "Spring Boot", "Docker", "Kubernetes", "AWS")))
                .andExpect(jsonPath("$.isReviewed").value(false))
                .andReturn();

        Long analysisId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        ResumeAnalysis savedAnalysis = resumeAnalysisRepository.findById(analysisId).orElseThrow();
        assertEquals(ResumeAnalysisStatus.ANALYZED, savedAnalysis.getStatus());
        assertNotNull(savedAnalysis.getExtractedText());
        assertNotNull(savedAnalysis.getSha256Hash());
        assertNotNull(savedAnalysis.getStorageKey());
    }

    // =========================================================================
    // 2. VALID DOCX UPLOAD & PARSING
    // =========================================================================

    @Test
    @DisplayName("Should upload valid DOCX, validate zip magic bytes, extract text, and generate structured analysis")
    void testUploadValidDocx() throws Exception {
        byte[] docxBytes = createTestDocxBytes(List.of(
                "Jordan Lee",
                "jordan.lee@example.com",
                "Lead Solutions Architect",
                "Skills: Python, TypeScript, React, PostgreSQL, Redis, Kafka",
                "Experience: Staff Engineer at Tech Global (2019 - Present)"
        ));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "jordan_lee_cv.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes
        );

        mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ANALYZED"))
                .andExpect(jsonPath("$.originalFilename").value("jordan_lee_cv.docx"))
                .andExpect(jsonPath("$.structuredData.skills", hasItems("Python", "TypeScript", "React", "PostgreSQL", "Redis", "Kafka")));
    }

    // =========================================================================
    // 3. SECURITY: MAGIC BYTES MISMATCH (SPOOFED FILE)
    // =========================================================================

    @Test
    @DisplayName("Should reject file whose extension is .pdf but content is not a real PDF (magic bytes mismatch)")
    void testRejectSpoofedPdfFile() throws Exception {
        byte[] fakeContent = "This is plain text masquerading as a PDF file.".getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake_resume.pdf",
                "application/pdf",
                fakeContent
        );

        mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("File signature mismatch")));
    }

    // =========================================================================
    // 4. SECURITY: PATH TRAVERSAL ATTEMPT
    // =========================================================================

    @Test
    @DisplayName("Should reject filename containing directory traversal sequences (../)")
    void testRejectPathTraversalFilename() throws Exception {
        byte[] pdfBytes = createTestPdfBytes("Valid content");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../../../etc/passwd.pdf",
                "application/pdf",
                pdfBytes
        );

        mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("path traversal")));
    }

    // =========================================================================
    // 5. SECURITY: DANGEROUS EXECUTABLE EXTENSION
    // =========================================================================

    @Test
    @DisplayName("Should reject dangerous executable file extensions (.exe)")
    void testRejectExecutableFile() throws Exception {
        byte[] exeHeader = new byte[]{0x4D, 0x5A, 0x00, 0x00}; // MZ executable header

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.exe",
                "application/x-msdownload",
                exeHeader
        );

        mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Executable or script files are strictly prohibited")));
    }

    // =========================================================================
    // 6. VALIDATION: OVERSIZED FILE (>10MB)
    // =========================================================================

    @Test
    @DisplayName("Should reject uploaded file exceeding the 10MB limit")
    void testRejectOversizedFile() throws Exception {
        byte[] oversizedData = new byte[11 * 1024 * 1024]; // 11MB
        oversizedData[0] = 0x25; oversizedData[1] = 0x50; oversizedData[2] = 0x44; oversizedData[3] = 0x46; // %PDF

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "oversized.pdf",
                "application/pdf",
                oversizedData
        );

        mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("exceeds maximum allowed threshold of 10MB")));
    }

    // =========================================================================
    // 7. EXTRACTION FAILURE / EMPTY DOCUMENT
    // =========================================================================

    @Test
    @DisplayName("Should record extraction failure when PDF contains no text (e.g. empty or scanned)")
    void testEmptyScannedPdfExtractionFailure() throws Exception {
        byte[] emptyPdf = createBlankPdfBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "blank.pdf",
                "application/pdf",
                emptyPdf
        );

        mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureReason", containsString("No readable text found in document")));
    }

    // =========================================================================
    // 8. RESILIENT AI FAILURE HANDLING
    // =========================================================================

    @Test
    @DisplayName("Should gracefully handle AI provider outage/timeout without crashing core application")
    void testAiProviderFailureGracefulHandling() throws Exception {
        doThrow(new RuntimeException("AI Provider API Gateway Timeout 504"))
                .when(structuredResumeAnalyzer).analyzeText(anyString());

        byte[] pdfBytes = createTestPdfBytes("Alex Rivera\nSoftware Engineer with Java experience");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "alex_test.pdf",
                "application/pdf",
                pdfBytes
        );

        mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureReason", containsString("AI analysis failed: AI Provider API Gateway Timeout 504")));
    }

    // =========================================================================
    // 9. CANDIDATE REVIEW & EXPLICIT PROFILE APPLY FLOW
    // =========================================================================

    @Test
    @DisplayName("Should allow candidate to review analysis and apply confirmed structured data to their profile")
    void testCandidateReviewAndApply() throws Exception {
        byte[] pdfBytes = createTestPdfBytes("Alex Rivera\nalex.rivera@example.com\nSenior Backend Engineer\nSkills: Java, Docker, AWS\nExperience: Senior Engineer at Acme (2021 - Present)");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "alex_resume.pdf",
                "application/pdf",
                pdfBytes
        );

        // Step 1: Upload and extract
        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ANALYZED"))
                .andReturn();

        Long analysisId = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();

        // Step 2: Candidate reviews analysis output
        mockMvc.perform(get("/api/v1/resumes/{id}", analysisId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(analysisId))
                .andExpect(jsonPath("$.isReviewed").value(false));

        // Step 3: Candidate modifies/confirms structured data and applies to profile
        StructuredResumeData confirmedData = new StructuredResumeData();
        confirmedData.setCurrentTitle("Staff Cloud Engineer");
        confirmedData.setSkills(List.of("Java", "Docker", "AWS", "Terraform"));

        mockMvc.perform(post("/api/v1/resumes/{id}/apply", analysisId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmedData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.skills[*].name", hasItems("Java", "Docker", "AWS", "Terraform")));

        // Verify analysis record is marked as APPLIED & reviewed
        ResumeAnalysis updatedAnalysis = resumeAnalysisRepository.findById(analysisId).orElseThrow();
        assertEquals(ResumeAnalysisStatus.APPLIED, updatedAnalysis.getStatus());
        assertTrue(updatedAnalysis.getIsReviewed());
    }

    // =========================================================================
    // 10. FILE PREVIEW / DOWNLOAD
    // =========================================================================

    @Test
    @DisplayName("Should securely retrieve stored resume file for candidate/recruiter preview")
    void testDownloadResumeFile() throws Exception {
        byte[] pdfBytes = createTestPdfBytes("Sample Resume Content for Download Test");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "download_sample.pdf",
                "application/pdf",
                pdfBytes
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString()))
                .andExpect(status().isCreated())
                .andReturn();

        Long analysisId = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/resumes/{id}/download", analysisId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("download_sample.pdf")));
    }

    // =========================================================================
    // TEST UTILITIES
    // =========================================================================

    private byte[] createTestPdfBytes(String text) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(50, 700);

                String[] lines = text.split("\n");
                for (String line : lines) {
                    cs.showText(line.replaceAll("[^\\x20-\\x7E]", " "));
                    cs.newLineAtOffset(0, -18);
                }
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createBlankPdfBytes() throws IOException {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createTestDocxBytes(List<String> lines) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            for (String line : lines) {
                XWPFParagraph p = document.createParagraph();
                p.createRun().setText(line);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            return baos.toByteArray();
        }
    }
}
