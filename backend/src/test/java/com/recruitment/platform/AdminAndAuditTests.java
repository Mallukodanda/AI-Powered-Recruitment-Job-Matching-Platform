package com.recruitment.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.platform.dto.PlatformAnalyticsDto;
import com.recruitment.platform.dto.UserResponse;
import com.recruitment.platform.model.*;
import com.recruitment.platform.repository.*;
import com.recruitment.platform.service.AdminService;
import com.recruitment.platform.service.AuditService;
import com.recruitment.platform.service.CompanyService;
import com.recruitment.platform.service.PlatformAnalyticsService;
import com.recruitment.platform.service.PlatformConfigService;
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
class AdminAndAuditTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PlatformConfigRepository platformConfigRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @Autowired
    private CandidateEmbeddingRepository candidateEmbeddingRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobAnalysisRepository jobAnalysisRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PlatformAnalyticsService platformAnalyticsService;

    @Autowired
    private PlatformConfigService platformConfigService;

    private User adminUser;
    private User recruiterUser;
    private User candidateUser;
    private Job testJob;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        platformConfigRepository.deleteAll();
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

        // 1. Seed Users with distinct roles
        adminUser = new User("admin.boss@example.com", "Chief Admin", "hashPass", Role.ADMIN);
        adminUser = userRepository.save(adminUser);

        recruiterUser = new User("recruiter.rob@example.com", "Rob Recruiter", "hashPass", Role.RECRUITER);
        recruiterUser = userRepository.save(recruiterUser);

        candidateUser = new User("candidate.claire@example.com", "Claire Dev", "hashPass", Role.CANDIDATE);
        candidateUser = userRepository.save(candidateUser);

        // 2. Seed Company
        testCompany = new Company("Nexus Technologies", "Cloud Infrastructure", "https://nexus.example.com",
                "San Francisco, CA", "hr@nexus.example.com", "Enterprise cloud data platform.");
        testCompany = companyRepository.save(testCompany);

        // 3. Seed Job
        testJob = new Job();
        testJob.setTitle("Staff Cloud Architect");
        testJob.setDepartment("Infrastructure");
        testJob.setLocation("San Francisco, CA");
        testJob.setCompany(testCompany);
        testJob.setCompanyName(testCompany.getName());
        testJob.setStatus("ACTIVE");
        testJob.setSkills("Java, Kubernetes, Docker, PostgreSQL");
        testJob = jobRepository.save(testJob);

        // Seed initial platform configs
        platformConfigService.initDefaultConfigs();
    }

    // =========================================================================
    // 1. ADMIN AUTHORIZATION & NON-ADMIN ACCESS DENIAL TESTS
    // =========================================================================

    @Test
    @DisplayName("Admin can list all platform users with pagination")
    void testAdminCanListUsers() {
        Page<UserResponse> page = adminService.listUsers(null, null, PageRequest.of(0, 10), adminUser.getEmail());

        assertNotNull(page);
        assertTrue(page.getTotalElements() >= 3);
        assertTrue(page.getContent().stream().anyMatch(u -> u.getEmail().equals(adminUser.getEmail())));
        assertTrue(page.getContent().stream().anyMatch(u -> u.getEmail().equals(recruiterUser.getEmail())));
        assertTrue(page.getContent().stream().anyMatch(u -> u.getEmail().equals(candidateUser.getEmail())));
    }

    @Test
    @DisplayName("Non-admin user (CANDIDATE or RECRUITER) is strictly denied access to admin endpoints")
    void testNonAdminDeniedAccessToAdminEndpoints() {
        // Candidate attempt
        AccessDeniedException candidateEx = assertThrows(AccessDeniedException.class, () ->
                adminService.listUsers(null, null, PageRequest.of(0, 10), candidateUser.getEmail())
        );
        assertTrue(candidateEx.getMessage().contains("Administrator privileges required"));

        // Recruiter attempt
        AccessDeniedException recruiterEx = assertThrows(AccessDeniedException.class, () ->
                adminService.listUsers(null, null, PageRequest.of(0, 10), recruiterUser.getEmail())
        );
        assertTrue(recruiterEx.getMessage().contains("Administrator privileges required"));

        // Verify security audit log recorded the unauthorized access attempt
        List<AuditLog> securityLogs = auditLogRepository.findAll();
        assertTrue(securityLogs.stream().anyMatch(l ->
                "UNAUTHORIZED_ADMIN_ACCESS_ATTEMPT".equals(l.getAction()) &&
                l.getActorEmail().equals(candidateUser.getEmail()) &&
                "FAILURE".equals(l.getResult())
        ));
    }

    @Test
    @DisplayName("Unauthenticated access to admin methods throws AccessDeniedException")
    void testUnauthenticatedAccessDenied() {
        assertThrows(AccessDeniedException.class, () ->
                adminService.listUsers(null, null, PageRequest.of(0, 10), null)
        );
    }

    // =========================================================================
    // 2. SELF-PROTECTION & BUSINESS RULES
    // =========================================================================

    @Test
    @DisplayName("Admin cannot demote their own account (prevents lockout)")
    void testAdminCannotDemoteThemselves() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateUserRole(adminUser.getId(), Role.CANDIDATE, adminUser.getEmail())
        );
        assertTrue(ex.getMessage().contains("cannot demote their own account"));

        // Verify role unchanged in repository
        User refreshed = userRepository.findById(adminUser.getId()).orElseThrow();
        assertEquals(Role.ADMIN, refreshed.getRole());
    }

    @Test
    @DisplayName("Admin cannot deactivate their own active account")
    void testAdminCannotDeactivateThemselves() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateUserStatus(adminUser.getId(), false, adminUser.getEmail())
        );
        assertTrue(ex.getMessage().contains("cannot deactivate their own active account"));

        User refreshed = userRepository.findById(adminUser.getId()).orElseThrow();
        assertTrue(refreshed.isActive());
    }

    @Test
    @DisplayName("Admin cannot delete their own active account")
    void testAdminCannotDeleteThemselves() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminService.deleteUser(adminUser.getId(), adminUser.getEmail())
        );
        assertTrue(ex.getMessage().contains("cannot delete their own active account"));

        assertTrue(userRepository.existsById(adminUser.getId()));
    }

    // =========================================================================
    // 3. USER MANAGEMENT & AUDITING
    // =========================================================================

    @Test
    @DisplayName("Admin can promote user role and toggle active status with audit trail")
    void testAdminCanUpdateUserRoleAndStatus() {
        // Promote candidate to RECRUITER
        UserResponse promoted = adminService.updateUserRole(candidateUser.getId(), Role.RECRUITER, adminUser.getEmail());
        assertEquals(Role.RECRUITER, promoted.getRole());

        // Deactivate candidate
        UserResponse deactivated = adminService.updateUserStatus(candidateUser.getId(), false, adminUser.getEmail());
        assertFalse(deactivated.isActive());

        // Verify audit logs were generated
        List<AuditLog> logs = auditLogRepository.findAll();
        assertTrue(logs.stream().anyMatch(l -> "USER_ROLE_UPDATED".equals(l.getAction())));
        assertTrue(logs.stream().anyMatch(l -> "USER_STATUS_UPDATED".equals(l.getAction())));
    }

    // =========================================================================
    // 4. SENSITIVE DATA PROTECTION IN AUDIT LOGGING
    // =========================================================================

    @Test
    @DisplayName("Sensitive credentials (passwords, tokens, API keys) must be sanitized and redacted in audit logs")
    void testSensitiveDataScrubbedFromAuditLogs() {
        String sensitiveJson = "{\"username\": \"john\", \"password\": \"SuperSecret123!\", \"apiKey\": \"ak_live_83921938\", \"token\": \"jwt.header.payload\"}";

        AuditLog log = auditService.logEvent(
                adminUser.getEmail(),
                "SECURITY_TEST",
                "SECURITY",
                "123",
                "SUCCESS",
                sensitiveJson,
                "127.0.0.1"
        );

        assertNotNull(log);
        assertNotNull(log.getMetadata());
        assertFalse(log.getMetadata().contains("SuperSecret123!"));
        assertFalse(log.getMetadata().contains("ak_live_83921938"));
        assertFalse(log.getMetadata().contains("jwt.header.payload"));
        assertTrue(log.getMetadata().contains("[REDACTED]"));
    }

    // =========================================================================
    // 5. COMPANY & JOB GOVERNANCE
    // =========================================================================

    @Test
    @DisplayName("Admin can register company and toggle verified status with audit log")
    void testCompanyCreationAndVerification() {
        Company newCo = new Company("Starlight Systems", "AI & Robotics", "https://starlight.ai",
                "Austin, TX", "contact@starlight.ai", "Autonomous AI software.");

        Company created = companyService.createCompany(newCo, adminUser.getEmail());
        assertNotNull(created.getId());
        assertEquals("Starlight Systems", created.getName());
        assertTrue(created.isVerified());

        // Revoke verification
        Company unverified = companyService.verifyCompany(created.getId(), false, adminUser.getEmail());
        assertFalse(unverified.isVerified());

        // Verify audit log
        List<AuditLog> logs = auditLogRepository.findAll();
        assertTrue(logs.stream().anyMatch(l -> "COMPANY_CREATED".equals(l.getAction())));
        assertTrue(logs.stream().anyMatch(l -> "COMPANY_UNVERIFIED".equals(l.getAction())));
    }

    @Test
    @DisplayName("Admin can oversee and update job status across the platform")
    void testAdminCanUpdateJobStatus() {
        Job updated = adminService.updateJobStatus(testJob.getId(), "CLOSED", adminUser.getEmail());
        assertEquals("CLOSED", updated.getStatus());

        Job inDb = jobRepository.findById(testJob.getId()).orElseThrow();
        assertEquals("CLOSED", inDb.getStatus());

        List<AuditLog> logs = auditLogRepository.findAll();
        assertTrue(logs.stream().anyMatch(l -> "JOB_STATUS_UPDATED".equals(l.getAction())));
    }

    // =========================================================================
    // 6. OPTIMIZED PLATFORM ANALYTICS AGGREGATIONS
    // =========================================================================

    @Test
    @DisplayName("Platform analytics computes accurate aggregates without loading whole tables")
    void testPlatformAnalyticsAggregations() {
        // Create candidate & application for analytics verification
        Candidate candidate = new Candidate("Test Analyst", "analyst@example.com", "+1-555-9000",
                "Data Analyst", 3, "SQL, Python, Tableau");
        candidate = candidateRepository.save(candidate);

        Application app = new Application();
        app.setJob(testJob);
        app.setCandidate(candidate);
        app.setStatus(ApplicationStatus.SHORTLISTED);
        app.setAiMatchScore(92.5);
        applicationRepository.save(app);

        PlatformAnalyticsDto analytics = platformAnalyticsService.getPlatformAnalytics();

        assertNotNull(analytics);
        assertTrue(analytics.getTotalUsers() >= 3);
        assertTrue(analytics.getTotalCandidates() >= 1);
        assertTrue(analytics.getTotalRecruiters() >= 1);
        assertTrue(analytics.getTotalAdmins() >= 1);
        assertTrue(analytics.getTotalCompanies() >= 1);
        assertTrue(analytics.getTotalJobs() >= 1);
        assertTrue(analytics.getTotalApplications() >= 1);

        // Group by status verification
        assertNotNull(analytics.getApplicationsByStatus());
        assertTrue(analytics.getApplicationsByStatus().containsKey("SHORTLISTED"));
        assertEquals(1L, analytics.getApplicationsByStatus().get("SHORTLISTED"));

        // AI metrics
        assertTrue(analytics.getAverageMatchScore() > 0.0);
        assertTrue(analytics.getAutoShortlistedCount() >= 1);
    }

    // =========================================================================
    // 7. PLATFORM CONFIGURATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Admin can update dynamic platform configurations with audit logging")
    void testDynamicPlatformConfigUpdate() {
        PlatformConfig updated = platformConfigService.updateConfig(
                "AI_AUTO_SHORTLIST_THRESHOLD", "85.0", adminUser.getEmail());

        assertEquals("85.0", updated.getConfigValue());
        assertEquals(adminUser.getEmail(), updated.getUpdatedBy());

        // Fast retrieval
        String val = platformConfigService.getConfig("AI_AUTO_SHORTLIST_THRESHOLD", "75.0");
        assertEquals("85.0", val);

        List<AuditLog> logs = auditLogRepository.findAll();
        assertTrue(logs.stream().anyMatch(l -> "CONFIG_UPDATED".equals(l.getAction())));
    }

    // =========================================================================
    // 8. REST CONTROLLER INTEGRATION TESTS (MOCKMVC)
    // =========================================================================

    @Test
    @DisplayName("REST API: GET /api/v1/analytics/platform returns 200 with platform metrics")
    void testRestApiPlatformAnalytics() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/platform"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.totalCompanies", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalJobs", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("REST API: GET /api/v1/companies returns 200 with paginated companies")
    void testRestApiListCompanies() throws Exception {
        mockMvc.perform(get("/api/v1/companies")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[0].name", is(testCompany.getName())));
    }
}
