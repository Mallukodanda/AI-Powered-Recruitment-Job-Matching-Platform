package com.recruitment.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.platform.dto.*;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.ProficiencyLevel;
import com.recruitment.platform.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@SuppressWarnings("null")
class CandidateModuleTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private com.recruitment.platform.repository.CandidateEmbeddingRepository candidateEmbeddingRepository;

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
        candidateRepository.deleteAll();
        testCandidate = new Candidate(
                "Jordan Lee",
                "jordan.lee@example.com",
                "+1-555-0199",
                "Senior Backend Engineer",
                5,
                "Java, Spring Boot, PostgreSQL"
        );
        testCandidate.setLocation("Austin, TX");
        testCandidate.setHeadline("Cloud Systems & Distributed Architect");
        testCandidate = candidateRepository.save(testCandidate);
    }

    @Test
    @DisplayName("Should retrieve complete candidate profile with empty sub-collections initially")
    void testGetCandidateProfile() throws Exception {
        mockMvc.perform(get("/api/v1/candidates/{id}/profile", testCandidate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCandidate.getId()))
                .andExpect(jsonPath("$.fullName").value("Jordan Lee"))
                .andExpect(jsonPath("$.email").value("jordan.lee@example.com"))
                .andExpect(jsonPath("$.educations").isArray())
                .andExpect(jsonPath("$.experiences").isArray())
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.certifications").isArray())
                .andExpect(jsonPath("$.projects").isArray());
    }

    @Test
    @DisplayName("Should update candidate profile metadata")
    void testUpdateCandidateProfile() throws Exception {
        CandidateProfileUpdateRequest updateRequest = new CandidateProfileUpdateRequest();
        updateRequest.setFullName("Jordan Lee, Ph.D.");
        updateRequest.setHeadline("Staff Distributed Systems Engineer");
        updateRequest.setLocation("Seattle, WA");
        updateRequest.setYearsExperience(7);
        updateRequest.setGithubUrl("https://github.com/jordanlee");

        mockMvc.perform(put("/api/v1/candidates/{id}/profile", testCandidate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Jordan Lee, Ph.D."))
                .andExpect(jsonPath("$.headline").value("Staff Distributed Systems Engineer"))
                .andExpect(jsonPath("$.location").value("Seattle, WA"))
                .andExpect(jsonPath("$.yearsExperience").value(7))
                .andExpect(jsonPath("$.githubUrl").value("https://github.com/jordanlee"));
    }

    @Test
    @DisplayName("Should add, update, and delete candidate Education")
    void testEducationCrud() throws Exception {
        EducationDto educationDto = new EducationDto(
                null,
                "Carnegie Mellon University",
                "Master of Science",
                "Computer Science",
                LocalDate.of(2018, 9, 1),
                LocalDate.of(2020, 5, 20),
                false,
                "3.9 GPA",
                "Thesis on Distributed Transactions"
        );

        // 1. Create Education
        String createResponse = mockMvc.perform(post("/api/v1/candidates/{candidateId}/education", testCandidate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(educationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.institution").value("Carnegie Mellon University"))
                .andExpect(jsonPath("$.degree").value("Master of Science"))
                .andReturn().getResponse().getContentAsString();

        EducationDto createdEducation = objectMapper.readValue(createResponse, EducationDto.class);
        Long educationId = createdEducation.getId();

        // 2. Update Education
        createdEducation.setGrade("4.0 GPA");
        mockMvc.perform(put("/api/v1/candidates/{candidateId}/education/{educationId}", testCandidate.getId(), educationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdEducation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("4.0 GPA"));

        // 3. Delete Education
        mockMvc.perform(delete("/api/v1/candidates/{candidateId}/education/{educationId}", testCandidate.getId(), educationId))
                .andExpect(status().isNoContent());

        // 4. Verify in complete profile
        mockMvc.perform(get("/api/v1/candidates/{id}/profile", testCandidate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.educations", hasSize(0)));
    }

    @Test
    @DisplayName("Should add, update, and delete candidate Experience")
    void testExperienceCrud() throws Exception {
        ExperienceDto experienceDto = new ExperienceDto(
                null,
                "Stripe",
                "Senior Software Engineer",
                "San Francisco, CA / Remote",
                "FULL_TIME",
                LocalDate.of(2021, 1, 15),
                null,
                true,
                "Led ledger microservices scaling to 50k RPS"
        );

        // 1. Create Experience
        String response = mockMvc.perform(post("/api/v1/candidates/{candidateId}/experience", testCandidate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(experienceDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.company").value("Stripe"))
                .andExpect(jsonPath("$.isCurrent").value(true))
                .andReturn().getResponse().getContentAsString();

        ExperienceDto created = objectMapper.readValue(response, ExperienceDto.class);
        Long expId = created.getId();

        // 2. Update Experience
        created.setTitle("Staff Infrastructure Engineer");
        mockMvc.perform(put("/api/v1/candidates/{candidateId}/experience/{expId}", testCandidate.getId(), expId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Staff Infrastructure Engineer"));

        // 3. Delete Experience
        mockMvc.perform(delete("/api/v1/candidates/{candidateId}/experience/{expId}", testCandidate.getId(), expId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should add and delete candidate Skills")
    void testSkillCrud() throws Exception {
        CandidateSkillDto skillDto = new CandidateSkillDto(
                null,
                "Spring Boot",
                "BACKEND",
                ProficiencyLevel.EXPERT,
                6
        );

        String response = mockMvc.perform(post("/api/v1/candidates/{candidateId}/skills", testCandidate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(skillDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Spring Boot"))
                .andExpect(jsonPath("$.proficiencyLevel").value("EXPERT"))
                .andReturn().getResponse().getContentAsString();

        CandidateSkillDto created = objectMapper.readValue(response, CandidateSkillDto.class);

        // Delete skill
        mockMvc.perform(delete("/api/v1/candidates/{candidateId}/skills/{skillId}", testCandidate.getId(), created.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should add and delete candidate Certifications")
    void testCertificationCrud() throws Exception {
        CertificationDto certDto = new CertificationDto(
                null,
                "AWS Solutions Architect Professional",
                "Amazon Web Services",
                LocalDate.of(2023, 4, 10),
                LocalDate.of(2026, 4, 10),
                "AWS-SAP-89231",
                "https://aws.amazon.com/verify/AWS-SAP-89231"
        );

        String response = mockMvc.perform(post("/api/v1/candidates/{candidateId}/certifications", testCandidate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("AWS Solutions Architect Professional"))
                .andExpect(jsonPath("$.issuingOrganization").value("Amazon Web Services"))
                .andReturn().getResponse().getContentAsString();

        CertificationDto created = objectMapper.readValue(response, CertificationDto.class);

        mockMvc.perform(delete("/api/v1/candidates/{candidateId}/certifications/{certId}", testCandidate.getId(), created.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should add and delete candidate Projects")
    void testProjectCrud() throws Exception {
        ProjectDto projectDto = new ProjectDto(
                null,
                "High-Throughput Payment Engine",
                "Built an idempotent payment processing engine handling 100M daily events",
                "Java 21, Spring Boot, Kafka, Redis, PostgreSQL",
                "https://payments.example.io",
                "https://github.com/jordanlee/payment-engine",
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 7, 1)
        );

        String response = mockMvc.perform(post("/api/v1/candidates/{candidateId}/projects", testCandidate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("High-Throughput Payment Engine"))
                .andReturn().getResponse().getContentAsString();

        ProjectDto created = objectMapper.readValue(response, ProjectDto.class);

        mockMvc.perform(delete("/api/v1/candidates/{candidateId}/projects/{projectId}", testCandidate.getId(), created.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when querying non-existent candidate profile")
    void testProfileNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/candidates/{id}/profile", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("Should paginate candidate summary results")
    void testPaginatedCandidateSearch() throws Exception {
        mockMvc.perform(get("/api/v1/candidates/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("search", "Jordan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].fullName").value("Jordan Lee"))
                .andExpect(jsonPath("$.totalElements").isNumber());
    }
}
