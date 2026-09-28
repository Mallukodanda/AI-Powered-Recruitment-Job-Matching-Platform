package com.recruitment.platform.service;

import com.recruitment.platform.dto.InterviewResponseDto;
import com.recruitment.platform.dto.InterviewScheduleRequest;
import com.recruitment.platform.dto.InterviewUpdateRequest;
import com.recruitment.platform.exception.ResourceNotFoundException;
import com.recruitment.platform.model.*;
import com.recruitment.platform.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationWorkflowService applicationWorkflowService;
    private final ApplicationEventPublisher eventPublisher;

    public InterviewService(InterviewRepository interviewRepository,
                            JobRepository jobRepository,
                            CandidateRepository candidateRepository,
                            UserRepository userRepository,
                            ApplicationRepository applicationRepository,
                            ApplicationWorkflowService applicationWorkflowService,
                            ApplicationEventPublisher eventPublisher) {
        this.interviewRepository = interviewRepository;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.applicationWorkflowService = applicationWorkflowService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Schedules a new interview with strict timezone validation, future date checks,
     * and interviewer conflict detection.
     */
    @Transactional
    public InterviewResponseDto scheduleInterview(InterviewScheduleRequest request, String callerEmail) {
        User caller = verifyRecruiterOrAdminAccess(callerEmail);

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + request.getCandidateId()));

        // Resolve Interviewer: from request or default to the authenticated recruiter
        User interviewer;
        if (request.getInterviewerId() != null) {
            interviewer = userRepository.findById(request.getInterviewerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Interviewer user not found with ID: " + request.getInterviewerId()));
        } else {
            interviewer = caller;
        }

        // Validate Timezone
        String timeZoneStr = (request.getTimeZone() != null && !request.getTimeZone().isBlank())
                ? request.getTimeZone().trim() : "UTC";
        try {
            ZoneId.of(timeZoneStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid IANA timezone identifier: " + timeZoneStr);
        }

        // Validate Scheduled Time is in the future
        ZonedDateTime scheduledTime = request.getScheduledTime();
        if (scheduledTime == null || scheduledTime.isBefore(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(1))) {
            throw new IllegalArgumentException("Interview must be scheduled for a future date and time.");
        }

        int duration = (request.getDurationMinutes() != null && request.getDurationMinutes() > 0)
                ? request.getDurationMinutes() : 60;

        // Check for Interviewer Calendar Conflicts
        ZonedDateTime slotStart = scheduledTime;
        ZonedDateTime slotEnd = scheduledTime.plusMinutes(duration);
        List<Interview> activeInterviews = interviewRepository.findByInterviewerIdAndStatus(
                interviewer.getId(), InterviewStatus.SCHEDULED);
        for (Interview existing : activeInterviews) {
            ZonedDateTime existStart = existing.getScheduledTime();
            int existDuration = existing.getDurationMinutes() != null ? existing.getDurationMinutes() : 60;
            ZonedDateTime existEnd = existStart.plusMinutes(existDuration);
            // Overlap condition: existStart < slotEnd && existEnd > slotStart
            if (existStart.isBefore(slotEnd) && existEnd.isAfter(slotStart)) {
                throw new IllegalArgumentException(String.format(
                        "Interviewer %s already has an active interview scheduled during this time slot.",
                        interviewer.getFullName()));
            }
        }

        // Create Interview Entity
        Interview interview = new Interview();
        interview.setJob(job);
        interview.setCandidate(candidate);
        interview.setInterviewer(interviewer);
        interview.setTitle(request.getTitle() != null ? request.getTitle() : "Recruitment Interview");
        interview.setInterviewType(request.getInterviewType() != null ? request.getInterviewType() : InterviewType.TECHNICAL);
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setScheduledTime(scheduledTime);
        interview.setTimeZone(timeZoneStr);
        interview.setDurationMinutes(duration);
        interview.setMeetingLink(request.getMeetingLink());
        interview.setRecruiterNotes(request.getNotes());

        // Link Application if available and advance pipeline state
        Application application = null;
        if (request.getApplicationId() != null) {
            application = applicationRepository.findById(request.getApplicationId()).orElse(null);
        }
        if (application == null) {
            application = applicationRepository.findByJobIdAndCandidateId(job.getId(), candidate.getId()).orElse(null);
        }

        if (application != null) {
            interview.setApplication(application);
            if (application.getStatus() != ApplicationStatus.INTERVIEW_SCHEDULED) {
                if (application.getStatus().isTerminal()) {
                    throw new com.recruitment.platform.exception.InvalidStateTransitionException(
                            "Cannot schedule interview for application in terminal state: " + application.getStatus());
                }
                if (application.getStatus() == ApplicationStatus.APPLIED ||
                        application.getStatus() == ApplicationStatus.UNDER_REVIEW ||
                        application.getStatus() == ApplicationStatus.AI_SCREENED) {
                    applicationWorkflowService.updateStatus(
                            application.getId(),
                            ApplicationStatus.SHORTLISTED,
                            "Shortlisted via recruiter interview scheduling"
                    );
                }
                applicationWorkflowService.updateStatus(
                        application.getId(),
                        ApplicationStatus.INTERVIEW_SCHEDULED,
                        "Interview scheduled: " + interview.getTitle()
                );
            }
        }

        Interview saved = interviewRepository.save(interview);

        // Asynchronously publish domain event to notify candidate and interviewer
        eventPublisher.publishEvent(new com.recruitment.platform.event.InterviewScheduledEvent(
                saved.getId(),
                candidate.getId(),
                candidate.getEmail(),
                candidate.getFullName(),
                job.getId(),
                job.getTitle(),
                interviewer.getId(),
                interviewer.getEmail(),
                interviewer.getFullName(),
                saved.getTitle(),
                saved.getScheduledTime(),
                saved.getTimeZone(),
                saved.getMeetingLink()
        ));

        return InterviewResponseDto.fromEntity(saved);
    }

    /**
     * Updates an interview's status, notes, feedback, or reschedules the appointment.
     */
    @Transactional
    public InterviewResponseDto updateInterview(Long interviewId, InterviewUpdateRequest request, String callerEmail) {
        verifyRecruiterOrAdminAccess(callerEmail);

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with ID: " + interviewId));

        if (request.getStatus() != null) {
            interview.setStatus(request.getStatus());
        }

        if (request.getScheduledTime() != null) {
            ZonedDateTime newTime = request.getScheduledTime();
            if (newTime.isBefore(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(1))) {
                throw new IllegalArgumentException("Rescheduled interview time must be in the future.");
            }
            interview.setScheduledTime(newTime);
            if (request.getStatus() == null) {
                interview.setStatus(InterviewStatus.RESCHEDULED);
            }
        }

        if (request.getTimeZone() != null && !request.getTimeZone().isBlank()) {
            try {
                ZoneId.of(request.getTimeZone().trim());
                interview.setTimeZone(request.getTimeZone().trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid IANA timezone identifier: " + request.getTimeZone());
            }
        }

        if (request.getDurationMinutes() != null && request.getDurationMinutes() > 0) {
            interview.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getMeetingLink() != null) {
            interview.setMeetingLink(request.getMeetingLink());
        }

        if (request.getNotes() != null) {
            interview.setRecruiterNotes(request.getNotes());
        }

        if (request.getInterviewerFeedback() != null) {
            interview.setInterviewerFeedback(request.getInterviewerFeedback());
        }

        if (request.getRating() != null) {
            if (request.getRating() < 1 || request.getRating() > 5) {
                throw new IllegalArgumentException("Interview rating must be between 1 and 5.");
            }
            interview.setRating(request.getRating());
        }

        Interview updated = interviewRepository.save(interview);

        // Asynchronously publish domain event to notify participants
        eventPublisher.publishEvent(new com.recruitment.platform.event.InterviewUpdatedEvent(
                updated.getId(),
                updated.getCandidate().getEmail(),
                updated.getInterviewer().getEmail(),
                updated.getJob().getTitle(),
                updated.getStatus(),
                updated.getScheduledTime(),
                updated.getTimeZone(),
                updated.getRecruiterNotes()
        ));

        return InterviewResponseDto.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public InterviewResponseDto getInterviewById(Long id, String callerEmail) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with ID: " + id));

        validateCandidateOrRecruiterAccess(interview, callerEmail);
        return InterviewResponseDto.fromEntity(interview);
    }

    @Transactional(readOnly = true)
    public List<InterviewResponseDto> getInterviewsForCandidate(Long candidateId, String callerEmail) {
        User caller = getUserOrThrow(callerEmail);
        if (caller.getRole() == Role.CANDIDATE) {
            Candidate candidate = candidateRepository.findByEmail(callerEmail)
                    .orElseThrow(() -> new AccessDeniedException("Candidate record not found for user: " + callerEmail));
            if (!candidate.getId().equals(candidateId)) {
                throw new AccessDeniedException("Access denied: You can only view your own scheduled interviews.");
            }
        }

        return interviewRepository.findByCandidateIdOrderByScheduledTimeAsc(candidateId)
                .stream()
                .map(InterviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewResponseDto> getInterviewsForJob(Long jobId, String callerEmail) {
        verifyRecruiterOrAdminAccess(callerEmail);
        return interviewRepository.findByJobIdOrderByScheduledTimeAsc(jobId)
                .stream()
                .map(InterviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewResponseDto> getAllInterviews(String callerEmail) {
        verifyRecruiterOrAdminAccess(callerEmail);
        return interviewRepository.findAll()
                .stream()
                .map(InterviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelInterview(Long interviewId, String callerEmail) {
        verifyRecruiterOrAdminAccess(callerEmail);
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with ID: " + interviewId));
        interview.setStatus(InterviewStatus.CANCELLED);
        Interview saved = interviewRepository.save(interview);

        eventPublisher.publishEvent(new com.recruitment.platform.event.InterviewUpdatedEvent(
                saved.getId(),
                saved.getCandidate().getEmail(),
                saved.getInterviewer().getEmail(),
                saved.getJob().getTitle(),
                InterviewStatus.CANCELLED,
                saved.getScheduledTime(),
                saved.getTimeZone(),
                "Interview cancelled by recruiter"
        ));
    }

    // =========================================================================
    // AUTHORIZATION HELPERS
    // =========================================================================

    private User verifyRecruiterOrAdminAccess(String callerEmail) {
        User user = getUserOrThrow(callerEmail);
        if (user.getRole() != Role.RECRUITER && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: Recruiter or Administrator role required.");
        }
        return user;
    }

    private void validateCandidateOrRecruiterAccess(Interview interview, String callerEmail) {
        User user = getUserOrThrow(callerEmail);
        if (user.getRole() == Role.RECRUITER || user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.CANDIDATE) {
            if (interview.getCandidate() != null &&
                    interview.getCandidate().getEmail() != null &&
                    interview.getCandidate().getEmail().equalsIgnoreCase(user.getEmail())) {
                return;
            }
        }
        throw new AccessDeniedException("Access denied: Not authorized to view this interview.");
    }

    private User getUserOrThrow(String callerEmail) {
        if (callerEmail == null || callerEmail.isBlank()) {
            throw new AccessDeniedException("Authentication required.");
        }
        return userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new AccessDeniedException("User not found with email: " + callerEmail));
    }
}
