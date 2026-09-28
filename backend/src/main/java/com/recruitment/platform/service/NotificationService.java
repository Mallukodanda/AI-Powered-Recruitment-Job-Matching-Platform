package com.recruitment.platform.service;

import com.recruitment.platform.dto.NotificationResponseDto;
import com.recruitment.platform.event.ApplicationStatusChangedEvent;
import com.recruitment.platform.event.InterviewScheduledEvent;
import com.recruitment.platform.event.InterviewUpdatedEvent;
import com.recruitment.platform.exception.ResourceNotFoundException;
import com.recruitment.platform.model.Notification;
import com.recruitment.platform.model.NotificationStatus;
import com.recruitment.platform.model.NotificationType;
import com.recruitment.platform.model.User;
import com.recruitment.platform.repository.NotificationRepository;
import com.recruitment.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Consumes application status change events and persists notifications with idempotency protection.
     */
    @EventListener
    @Transactional
    public void handleApplicationStatusChanged(ApplicationStatusChangedEvent event) {
        if (event == null) return;
        try {
            log.info("Processing ApplicationStatusChangedEvent [eventId={}]: application={}, candidate={}, status={}",
                    event.getEventId(), event.getApplicationId(), event.getCandidateEmail(), event.getNewStatus());

            Optional<User> recipientOpt = userRepository.findByEmail(event.getCandidateEmail());
            if (recipientOpt.isEmpty()) {
                log.warn("Notification skipped: Candidate user not found for email {}", event.getCandidateEmail());
                return;
            }

            User recipient = recipientOpt.get();

            // Idempotency check: prevent duplicate notifications for same event
            if (notificationRepository.existsByEventIdAndRecipientId(event.getEventId(), recipient.getId())) {
                log.info("Duplicate event ignored for recipient {}: eventId={}", recipient.getEmail(), event.getEventId());
                return;
            }

            NotificationType type = (event.getNewStatus() == com.recruitment.platform.model.ApplicationStatus.SHORTLISTED)
                    ? NotificationType.APPLICATION_SHORTLISTED
                    : NotificationType.APPLICATION_STATUS_CHANGED;

            String title = String.format("Application Update: %s", event.getJobTitle());
            String message = String.format("Your application for '%s' has moved to stage: %s.%s",
                    event.getJobTitle(),
                    event.getNewStatus(),
                    (event.getNotes() != null && !event.getNotes().isBlank()) ? " Note: " + event.getNotes() : "");

            Notification notification = new Notification(
                    recipient,
                    type,
                    title,
                    message,
                    "APPLICATION",
                    event.getApplicationId(),
                    event.getEventId()
            );

            notificationRepository.save(notification);
            log.info("Successfully delivered notification id={} to {}", notification.getId(), recipient.getEmail());
        } catch (Exception ex) {
            // Failure isolation: log error, ensure business operations continue
            log.error("Failed to process ApplicationStatusChangedEvent [eventId={}]: {}", event.getEventId(), ex.getMessage(), ex);
        }
    }

    /**
     * Consumes interview scheduled events and notifies both candidate and interviewer.
     */
    @EventListener
    @Transactional
    public void handleInterviewScheduled(InterviewScheduledEvent event) {
        if (event == null) return;
        try {
            log.info("Processing InterviewScheduledEvent [eventId={}]: interview={}, candidate={}, time={}",
                    event.getEventId(), event.getInterviewId(), event.getCandidateEmail(), event.getScheduledTime());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm z");
            String formattedTime = (event.getScheduledTime() != null)
                    ? event.getScheduledTime().format(formatter)
                    : "TBD";

            // 1. Notify Candidate
            Optional<User> candidateOpt = userRepository.findByEmail(event.getCandidateEmail());
            if (candidateOpt.isPresent()) {
                User candidateUser = candidateOpt.get();
                if (!notificationRepository.existsByEventIdAndRecipientId(event.getEventId(), candidateUser.getId())) {
                    String title = "Interview Scheduled: " + event.getJobTitle();
                    String message = String.format("Your %s for '%s' has been scheduled for %s (%s).%s",
                            event.getInterviewTitle(),
                            event.getJobTitle(),
                            formattedTime,
                            event.getTimeZone(),
                            (event.getMeetingLink() != null) ? " Meeting link: " + event.getMeetingLink() : "");

                    Notification notification = new Notification(
                            candidateUser,
                            NotificationType.INTERVIEW_SCHEDULED,
                            title,
                            message,
                            "INTERVIEW",
                            event.getInterviewId(),
                            event.getEventId()
                    );
                    notificationRepository.save(notification);
                }
            }

            // 2. Notify Interviewer
            if (event.getInterviewerEmail() != null) {
                Optional<User> interviewerOpt = userRepository.findByEmail(event.getInterviewerEmail());
                if (interviewerOpt.isPresent()) {
                    User interviewerUser = interviewerOpt.get();
                    if (!notificationRepository.existsByEventIdAndRecipientId(event.getEventId(), interviewerUser.getId())) {
                        String title = "Upcoming Interview: " + event.getCandidateName();
                        String message = String.format("You are scheduled to conduct %s with candidate %s for '%s' on %s.",
                                event.getInterviewTitle(),
                                event.getCandidateName(),
                                event.getJobTitle(),
                                formattedTime);

                        Notification notification = new Notification(
                                interviewerUser,
                                NotificationType.INTERVIEW_SCHEDULED,
                                title,
                                message,
                                "INTERVIEW",
                                event.getInterviewId(),
                                event.getEventId()
                        );
                        notificationRepository.save(notification);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to process InterviewScheduledEvent [eventId={}]: {}", event.getEventId(), ex.getMessage(), ex);
        }
    }

    /**
     * Consumes interview update events (rescheduled, cancelled, completed).
     */
    @EventListener
    @Transactional
    public void handleInterviewUpdated(InterviewUpdatedEvent event) {
        if (event == null) return;
        try {
            log.info("Processing InterviewUpdatedEvent [eventId={}]: interview={}, status={}",
                    event.getEventId(), event.getInterviewId(), event.getStatus());

            Optional<User> candidateOpt = userRepository.findByEmail(event.getCandidateEmail());
            if (candidateOpt.isPresent()) {
                User candidateUser = candidateOpt.get();
                if (!notificationRepository.existsByEventIdAndRecipientId(event.getEventId(), candidateUser.getId())) {
                    NotificationType type = (event.getStatus() == com.recruitment.platform.model.InterviewStatus.CANCELLED)
                            ? NotificationType.INTERVIEW_CANCELLED
                            : NotificationType.INTERVIEW_UPDATED;

                    String title = String.format("Interview Update: %s (%s)", event.getJobTitle(), event.getStatus());
                    String message = String.format("Your interview for '%s' status has been updated to %s.%s",
                            event.getJobTitle(),
                            event.getStatus(),
                            (event.getNotes() != null && !event.getNotes().isBlank()) ? " Details: " + event.getNotes() : "");

                    Notification notification = new Notification(
                            candidateUser,
                            type,
                            title,
                            message,
                            "INTERVIEW",
                            event.getInterviewId(),
                            event.getEventId()
                    );
                    notificationRepository.save(notification);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to process InterviewUpdatedEvent [eventId={}]: {}", event.getEventId(), ex.getMessage(), ex);
        }
    }

    // =========================================================================
    // USER NOTIFICATION MANAGEMENT
    // =========================================================================

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsForUser(String userEmail, NotificationStatus status) {
        User user = getUserOrThrow(userEmail);
        List<Notification> list = (status != null)
                ? notificationRepository.findByRecipientIdAndStatusOrderByCreatedAtDesc(user.getId(), status)
                : notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());

        return list.stream().map(NotificationResponseDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        User user = getUserOrThrow(userEmail);
        return notificationRepository.countByRecipientIdAndStatus(user.getId(), NotificationStatus.UNREAD);
    }

    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId, String userEmail) {
        User user = getUserOrThrow(userEmail);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied: You cannot modify notifications belonging to another user.");
        }

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        return NotificationResponseDto.fromEntity(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = getUserOrThrow(userEmail);
        List<Notification> unread = notificationRepository.findByRecipientIdAndStatusOrderByCreatedAtDesc(
                user.getId(), NotificationStatus.UNREAD);

        LocalDateTime now = LocalDateTime.now();
        unread.forEach(n -> {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
    }

    private User getUserOrThrow(String callerEmail) {
        if (callerEmail == null || callerEmail.isBlank()) {
            throw new AccessDeniedException("Authentication required.");
        }
        return userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new AccessDeniedException("User not found with email: " + callerEmail));
    }
}
