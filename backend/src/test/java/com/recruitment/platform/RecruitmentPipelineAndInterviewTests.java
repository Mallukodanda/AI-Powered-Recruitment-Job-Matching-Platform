package com.recruitment.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.platform.dto.InterviewPrepDto;
import com.recruitment.platform.dto.InterviewResponseDto;
import com.recruitment.platform.dto.InterviewScheduleRequest;
import com.recruitment.platform.dto.InterviewUpdateRequest;
import com.recruitment.platform.event.ApplicationStatusChangedEvent;
import com.recruitment.platform.exception.InvalidStateTransitionException;
import com.recruitment.platform.model.*;
import com.recruitment.platform.repository.*;
import com.recruitment.platform.service.ApplicationWorkflowService;
import com.recruitment.platform.service.InterviewService;
import com.recruitment.platform.service.NotificationService;
import com.recruitment.platform.service.ai.AiInterviewAssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@SuppressWarnings("null")
class RecruitmentPipelineAndInterviewTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private UserRepository userRepository;

    @Autowired
    private ApplicationWorkflowService applicationWorkflowService;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AiInterviewAssistantService aiInterviewAssistantService;

    private User recruiterUser;
    private User candidateUser;
    private Candidate testCandidate;
    private Job testJob;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        interviewRepository.deleteAll();
        applicationRepository.deleteAll();
        resumeAnalysisRepository.deleteAll();
        candidateEmbeddingRepository.deleteAll();
        candidateRepository.deleteAll();
        jobAnalysisRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Seed Recruiter User
        recruiterUser = new User("sarah.recruiter@example.com", "Sarah Connor", "passwordHash", Role.RECRUITER);
        recruiterUser = userRepository.save(recruiterUser);

        // 2. Seed Candidate User & Entity
        candidateUser = new User("alex.engineer@example.com", "Alex Engineer", "passwordHash", Role.CANDIDATE);
        candidateUser = userRepository.save(candidateUser);

        testCandidate = new Candidate();
        testCandidate.setFullName("Alex Engineer");
        testCandidate.setEmail("alex.engineer@example.com");
        testCandidate.setCurrentTitle("Senior Backend Engineer");
        testCandidate.setLocation("Remote");
        testCandidate.setYearsExperience(6);
        testCandidate.setHighestEducation("Bachelor of Science in Computer Science");
        testCandidate.setSkillsSummary("Java, Spring Boot, PostgreSQL, Docker, Microservices");
        testCandidate.setResumeText("6 years architecting enterprise distributed microservices using Spring Boot, PostgreSQL, and Docker containers.");
        testCandidate = candidateRepository.save(testCandidate);

        // 3. Seed Job
        testJob = new Job();
        testJob.setTitle("Principal Java Backend Architect");
        testJob.setDepartment("Engineering");
        testJob.setLocation("Remote");
        testJob.setJobType("FULL_TIME");
        testJob.setExperienceLevel("SENIOR");
        testJob.setMinExperienceYears(5);
        testJob.setDescription("Architect distributed event-driven systems and microservices with high throughput and low latency.");
        testJob.setRequirements("Strong experience with Java, Spring Boot, PostgreSQL, Docker, and Kafka messaging.");
        testJob.setSkills("Java, Spring Boot, PostgreSQL, Docker, Kafka");
        testJob = jobRepository.save(testJob);

        // 4. Seed Initial Application
        testApplication = new Application();
        testApplication.setJob(testJob);
        testApplication.setCandidate(testCandidate);
        testApplication.setStatus(ApplicationStatus.APPLIED);
        testApplication.setAiMatchScore(88.0);
        testApplication.setAiRationale("High alignment on Java, Spring Boot, and PostgreSQL.");
        testApplication = applicationRepository.save(testApplication);
    }

    // =========================================================================
    // 1. RECRUITMENT PIPELINE & STATE MACHINE TESTS
    // =========================================================================

    @Test
    @DisplayName("Should successfully advance application through valid recruitment pipeline sequence")
    void testValidApplicationProgression() {
        Long appId = testApplication.getId();

        // Step 1: APPLIED -> UNDER_REVIEW
        Application step1 = applicationWorkflowService.updateStatus(appId, ApplicationStatus.UNDER_REVIEW, "Recruiter screening candidate");
        assertEquals(ApplicationStatus.UNDER_REVIEW, step1.getStatus());

        // Step 2: UNDER_REVIEW -> SHORTLISTED
        Application step2 = applicationWorkflowService.updateStatus(appId, ApplicationStatus.SHORTLISTED, "Candidate shortlisted for technical evaluation");
        assertEquals(ApplicationStatus.SHORTLISTED, step2.getStatus());

        // Step 3: SHORTLISTED -> INTERVIEW_SCHEDULED
        Application step3 = applicationWorkflowService.updateStatus(appId, ApplicationStatus.INTERVIEW_SCHEDULED, "Technical round scheduled");
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, step3.getStatus());

        // Step 4: INTERVIEW_SCHEDULED -> OFFERED
        Application step4 = applicationWorkflowService.updateStatus(appId, ApplicationStatus.OFFERED, "Offer extended to candidate");
        assertEquals(ApplicationStatus.OFFERED, step4.getStatus());

        // Step 5: OFFERED -> HIRED
        Application step5 = applicationWorkflowService.updateStatus(appId, ApplicationStatus.HIRED, "Offer accepted. Welcome to the team!");
        assertEquals(ApplicationStatus.HIRED, step5.getStatus());
        assertTrue(step5.getStatus().isTerminal());
    }

    @Test
    @DisplayName("Should reject invalid state transition skipping required pipeline stages")
    void testInvalidStateTransitionBypasses() {
        Long appId = testApplication.getId();
        // Candidate is in APPLIED status. Direct jump to HIRED must be rejected!
        InvalidStateTransitionException ex = assertThrows(InvalidStateTransitionException.class, () ->
                applicationWorkflowService.updateStatus(appId, ApplicationStatus.HIRED, "Unauthorized shortcut")
        );
        assertEquals(ApplicationStatus.APPLIED, ex.getFromStatus());
        assertEquals(ApplicationStatus.HIRED, ex.getToStatus());

        // Verify status remains unchanged in repository
        Application unchanged = applicationRepository.findById(appId).orElseThrow();
        assertEquals(ApplicationStatus.APPLIED, unchanged.getStatus());
    }

    @Test
    @DisplayName("Should reject any transitions from terminal states HIRED and REJECTED")
    void testTerminalStateRejection() {
        Long appId = testApplication.getId();

        // Move to REJECTED terminal state
        Application rejected = applicationWorkflowService.updateStatus(appId, ApplicationStatus.REJECTED, "Candidate rejected");
        assertEquals(ApplicationStatus.REJECTED, rejected.getStatus());
        assertTrue(rejected.getStatus().isTerminal());

        // Any further transition from REJECTED must throw InvalidStateTransitionException
        assertThrows(InvalidStateTransitionException.class, () ->
                applicationWorkflowService.updateStatus(appId, ApplicationStatus.UNDER_REVIEW, "Attempting to reopen")
        );
    }

    // =========================================================================
    // 2. INTERVIEW SCHEDULING & TIMEZONE MANAGEMENT TESTS
    // =========================================================================

    @Test
    @DisplayName("Should successfully schedule interview with explicit timezone and calculate local representations")
    void testValidInterviewCreationWithTimezone() {
        ZonedDateTime scheduledTimeUtc = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(3);

        InterviewScheduleRequest request = new InterviewScheduleRequest();
        request.setJobId(testJob.getId());
        request.setCandidateId(testCandidate.getId());
        request.setInterviewerId(recruiterUser.getId());
        request.setApplicationId(testApplication.getId());
        request.setTitle("Technical Architecture Deep Dive");
        request.setInterviewType(InterviewType.SYSTEM_DESIGN);
        request.setScheduledTime(scheduledTimeUtc);
        request.setTimeZone("America/New_York");
        request.setDurationMinutes(60);
        request.setMeetingLink("https://meet.recruitment-platform.internal/room-456");
        request.setNotes("Focus on distributed caching and concurrency control.");

        InterviewResponseDto response = interviewService.scheduleInterview(request, recruiterUser.getEmail());

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Technical Architecture Deep Dive", response.getTitle());
        assertEquals(InterviewType.SYSTEM_DESIGN, response.getInterviewType());
        assertEquals(InterviewStatus.SCHEDULED, response.getStatus());
        assertEquals("America/New_York", response.getTimeZone());
        assertNotNull(response.getScheduledTimeFormattedUtc());
        assertNotNull(response.getScheduledTimeFormattedLocal());
        assertTrue(response.getScheduledTimeFormattedLocal().contains("EDT") ||
                   response.getScheduledTimeFormattedLocal().contains("EST") ||
                   response.getScheduledTimeFormattedLocal().contains("America/New_York"));

        // Application status must have automatically transitioned to INTERVIEW_SCHEDULED
        Application app = applicationRepository.findById(testApplication.getId()).orElseThrow();
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, app.getStatus());
    }

    @Test
    @DisplayName("Should reject scheduling interview with invalid IANA timezone")
    void testInterviewSchedulingWithInvalidTimezoneThrowsException() {
        InterviewScheduleRequest request = new InterviewScheduleRequest();
        request.setJobId(testJob.getId());
        request.setCandidateId(testCandidate.getId());
        request.setScheduledTime(ZonedDateTime.now(ZoneId.of("UTC")).plusDays(2));
        request.setTimeZone("Mars/Olympus_Mons");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                interviewService.scheduleInterview(request, recruiterUser.getEmail())
        );
        assertTrue(ex.getMessage().contains("Invalid IANA timezone identifier"));
    }

    @Test
    @DisplayName("Should detect interviewer double-booking conflict and reject overlapping appointment")
    void testInterviewerDoubleBookingConflictDetection() {
        ZonedDateTime slotTime = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(2).withHour(14).withMinute(0);

        // Schedule first interview
        InterviewScheduleRequest firstReq = new InterviewScheduleRequest();
        firstReq.setJobId(testJob.getId());
        firstReq.setCandidateId(testCandidate.getId());
        firstReq.setInterviewerId(recruiterUser.getId());
        firstReq.setScheduledTime(slotTime);
        firstReq.setTimeZone("UTC");
        firstReq.setDurationMinutes(60);
        interviewService.scheduleInterview(firstReq, recruiterUser.getEmail());

        // Attempt second interview for same interviewer during overlapping time slot
        InterviewScheduleRequest conflictReq = new InterviewScheduleRequest();
        conflictReq.setJobId(testJob.getId());
        conflictReq.setCandidateId(testCandidate.getId());
        conflictReq.setInterviewerId(recruiterUser.getId());
        conflictReq.setScheduledTime(slotTime.plusMinutes(30)); // 30 mins into the 60 min meeting!
        conflictReq.setTimeZone("UTC");
        conflictReq.setDurationMinutes(60);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                interviewService.scheduleInterview(conflictReq, recruiterUser.getEmail())
        );
        assertTrue(ex.getMessage().contains("already has an active interview scheduled during this time slot"));
    }

    @Test
    @DisplayName("Should reschedule and cancel interviews with status auditing")
    void testRescheduleAndCancelInterview() {
        ZonedDateTime originalTime = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(4);

        InterviewScheduleRequest request = new InterviewScheduleRequest();
        request.setJobId(testJob.getId());
        request.setCandidateId(testCandidate.getId());
        request.setInterviewerId(recruiterUser.getId());
        request.setScheduledTime(originalTime);
        request.setTimeZone("UTC");
        InterviewResponseDto scheduled = interviewService.scheduleInterview(request, recruiterUser.getEmail());

        // Reschedule
        ZonedDateTime newTime = originalTime.plusDays(1);
        InterviewUpdateRequest updateReq = new InterviewUpdateRequest();
        updateReq.setScheduledTime(newTime);
        updateReq.setStatus(InterviewStatus.RESCHEDULED);
        updateReq.setNotes("Candidate requested one-day postponement.");

        InterviewResponseDto rescheduled = interviewService.updateInterview(scheduled.getId(), updateReq, recruiterUser.getEmail());
        assertEquals(InterviewStatus.RESCHEDULED, rescheduled.getStatus());

        // Cancel
        interviewService.cancelInterview(scheduled.getId(), recruiterUser.getEmail());
        InterviewResponseDto cancelled = interviewService.getInterviewById(scheduled.getId(), recruiterUser.getEmail());
        assertEquals(InterviewStatus.CANCELLED, cancelled.getStatus());
    }

    // =========================================================================
    // 3. AUTHORIZATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Candidate cannot schedule an interview (Recruiter / Admin required)")
    void testCandidateCannotScheduleInterview() {
        InterviewScheduleRequest request = new InterviewScheduleRequest();
        request.setJobId(testJob.getId());
        request.setCandidateId(testCandidate.getId());
        request.setScheduledTime(ZonedDateTime.now(ZoneId.of("UTC")).plusDays(2));

        assertThrows(AccessDeniedException.class, () ->
                interviewService.scheduleInterview(request, candidateUser.getEmail())
        );
    }

    @Test
    @DisplayName("Candidate cannot view another candidate's private interviews")
    void testCandidateCannotViewOtherCandidatesInterviews() {
        // Create second candidate
        User otherCandidateUser = new User("other.candidate@example.com", "Other Candidate", "hash", Role.CANDIDATE);
        userRepository.save(otherCandidateUser);

        Candidate otherCandidate = new Candidate();
        otherCandidate.setFullName("Other Candidate");
        otherCandidate.setEmail("other.candidate@example.com");
        otherCandidate = candidateRepository.save(otherCandidate);

        // Schedule interview for Candidate 1
        InterviewScheduleRequest request = new InterviewScheduleRequest();
        request.setJobId(testJob.getId());
        request.setCandidateId(testCandidate.getId());
        request.setInterviewerId(recruiterUser.getId());
        request.setScheduledTime(ZonedDateTime.now(ZoneId.of("UTC")).plusDays(3));
        request.setTimeZone("UTC");
        interviewService.scheduleInterview(request, recruiterUser.getEmail());

        // Other candidate tries to list Candidate 1's interviews
        assertThrows(AccessDeniedException.class, () ->
                interviewService.getInterviewsForCandidate(testCandidate.getId(), otherCandidateUser.getEmail())
        );
    }

    // =========================================================================
    // 4. NOTIFICATIONS & ASYNCHRONOUS EVENT PROCESSING
    // =========================================================================

    @Test
    @DisplayName("Should generate notification for candidate upon pipeline status change")
    void testNotificationGeneratedOnStatusChange() {
        notificationRepository.deleteAll();

        // Trigger status change to SHORTLISTED
        applicationWorkflowService.updateStatus(testApplication.getId(), ApplicationStatus.SHORTLISTED, "Top 5% AI match profile");

        // Verify notification was asynchronously delivered and stored
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(candidateUser.getId());
        assertFalse(notifications.isEmpty());
        Notification notification = notifications.get(0);
        assertEquals(NotificationType.APPLICATION_SHORTLISTED, notification.getType());
        assertEquals(NotificationStatus.UNREAD, notification.getStatus());
        assertTrue(notification.getMessage().contains("SHORTLISTED"));
        assertNotNull(notification.getEventId());
    }

    @Test
    @DisplayName("Should suppress duplicate events through eventId idempotency check")
    void testDuplicateEventSuppressionIdempotency() {
        notificationRepository.deleteAll();

        String duplicateEventId = UUID.randomUUID().toString();
        ApplicationStatusChangedEvent event1 = new ApplicationStatusChangedEvent(
                duplicateEventId,
                testApplication.getId(),
                testCandidate.getId(),
                testCandidate.getEmail(),
                testCandidate.getFullName(),
                testJob.getId(),
                testJob.getTitle(),
                ApplicationStatus.APPLIED,
                ApplicationStatus.UNDER_REVIEW,
                "Under review",
                Instant.now()
        );

        ApplicationStatusChangedEvent event2 = new ApplicationStatusChangedEvent(
                duplicateEventId, // Identical EventId
                testApplication.getId(),
                testCandidate.getId(),
                testCandidate.getEmail(),
                testCandidate.getFullName(),
                testJob.getId(),
                testJob.getTitle(),
                ApplicationStatus.APPLIED,
                ApplicationStatus.UNDER_REVIEW,
                "Under review duplicate delivery",
                Instant.now()
        );

        // Deliver event twice
        notificationService.handleApplicationStatusChanged(event1);
        notificationService.handleApplicationStatusChanged(event2);

        // Only ONE notification should exist
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(candidateUser.getId());
        assertEquals(1, notifications.size());
    }

    @Test
    @DisplayName("Failed notification recipient does not break system and is safely isolated")
    void testFailedNotificationIsolation() {
        // Event with non-existent candidate email
        ApplicationStatusChangedEvent orphanEvent = new ApplicationStatusChangedEvent(
                UUID.randomUUID().toString(),
                999L,
                888L,
                "ghost.user@nonexistent-domain-404.com",
                "Ghost User",
                testJob.getId(),
                testJob.getTitle(),
                ApplicationStatus.APPLIED,
                ApplicationStatus.UNDER_REVIEW,
                "Orphan event",
                Instant.now()
        );

        // Handler must not throw exception
        assertDoesNotThrow(() -> notificationService.handleApplicationStatusChanged(orphanEvent));
    }

    // =========================================================================
    // 5. AI INTERVIEW PREPARATION ASSISTANT TESTS
    // =========================================================================

    @Test
    @DisplayName("AI Interview Assistant generates structured technical, role-specific, HR, and probing questions")
    void testAiInterviewPrepGenerationStructureAndContent() {
        InterviewPrepDto prep = aiInterviewAssistantService.generateInterviewPrep(testJob.getId(), testCandidate.getId());

        assertNotNull(prep);
        assertEquals(testCandidate.getId(), prep.getCandidateId());
        assertEquals(testJob.getId(), prep.getJobId());

        // Matched Skills vs Missing
        assertNotNull(prep.getMatchedSkills());
        assertTrue(prep.getMatchedSkills().contains("Java") || prep.getMatchedSkills().contains("Spring Boot"));

        // Preparation Topics
        assertNotNull(prep.getPreparationTopics());
        assertFalse(prep.getPreparationTopics().isEmpty());

        // Technical Questions
        assertNotNull(prep.getTechnicalQuestions());
        assertFalse(prep.getTechnicalQuestions().isEmpty());
        prep.getTechnicalQuestions().forEach(q -> {
            assertNotNull(q.getQuestion());
            assertNotNull(q.getTargetSkillOrTopic());
            assertNotNull(q.getExpectedConcepts());
            assertFalse(q.getExpectedConcepts().isEmpty());
            assertNotNull(q.getScoringCriteria());
        });

        // Role-Specific Questions
        assertNotNull(prep.getRoleSpecificQuestions());
        assertFalse(prep.getRoleSpecificQuestions().isEmpty());

        // HR Questions
        assertNotNull(prep.getHrQuestions());
        assertFalse(prep.getHrQuestions().isEmpty());

        // Follow-Up Questions (probing on Kafka or background)
        assertNotNull(prep.getFollowUpQuestions());
        assertFalse(prep.getFollowUpQuestions().isEmpty());

        // Non-autonomous hiring disclaimer
        assertNotNull(prep.getDisclaimer());
        assertTrue(prep.getDisclaimer().contains("ASSISTIVE PREPARATION TOOL ONLY"));
    }

    @Test
    @DisplayName("AI Assistant handles candidate without skills gracefully with standard fallbacks")
    void testAiAssistantHandlesCandidateWithNoExplicitSkillsGracefully() {
        Candidate blankCandidate = new Candidate();
        blankCandidate.setFullName("Entry Level");
        blankCandidate.setEmail("entry.level@example.com");
        blankCandidate.setYearsExperience(0);
        blankCandidate = candidateRepository.save(blankCandidate);

        InterviewPrepDto prep = aiInterviewAssistantService.generateInterviewPrep(testJob.getId(), blankCandidate.getId());

        assertNotNull(prep);
        assertNotNull(prep.getTechnicalQuestions());
        assertFalse(prep.getTechnicalQuestions().isEmpty());
        assertNotNull(prep.getHrQuestions());
        assertNotNull(prep.getPreparationTopics());
    }

    // =========================================================================
    // 6. REST API INTEGRATION TESTS VIA MOCKMVC
    // =========================================================================

    @Test
    @DisplayName("REST API: PATCH /api/v1/applications/{id}/status returns 400 on invalid state transition")
    void testRestApiInvalidStatusTransition() throws Exception {
        mockMvc.perform(patch("/api/v1/applications/" + testApplication.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of(
                        "status", "HIRED",
                        "notes", "Direct invalid jump"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid State Transition")))
                .andExpect(jsonPath("$.message", containsString("Invalid pipeline state transition")));
    }

    @Test
    @DisplayName("REST API: GET /api/v1/interviews/prep/{jobId}/{candidateId} returns 200 with structured prep")
    void testRestApiGetInterviewPrep() throws Exception {
        mockMvc.perform(get("/api/v1/interviews/prep/" + testJob.getId() + "/" + testCandidate.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle", is(testJob.getTitle())))
                .andExpect(jsonPath("$.technicalQuestions", not(empty())))
                .andExpect(jsonPath("$.hrQuestions", not(empty())))
                .andExpect(jsonPath("$.disclaimer", containsString("ASSISTIVE PREPARATION TOOL ONLY")));
    }
}
