package com.recruitment.platform.service;

import com.recruitment.platform.dto.UserResponse;
import com.recruitment.platform.exception.ResourceNotFoundException;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.model.Role;
import com.recruitment.platform.model.User;
import com.recruitment.platform.repository.JobRepository;
import com.recruitment.platform.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class AdminService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final AuditService auditService;

    public AdminService(UserRepository userRepository,
                        JobRepository jobRepository,
                        AuditService auditService) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.auditService = auditService;
    }

    // =========================================================================
    // USER MANAGEMENT
    // =========================================================================

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Role roleFilter, String search, Pageable pageable, String callerEmail) {
        verifyAdminAccess(callerEmail);

        Page<User> users;
        if (roleFilter != null) {
            users = userRepository.findAll(pageable); // In practice or with specification
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(UserResponse::fromEntity);
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, Role newRole, String callerEmail) {
        User admin = verifyAdminAccess(callerEmail);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Self-protection business rule: An admin cannot demote themselves
        if (target.getId().equals(admin.getId()) && newRole != Role.ADMIN) {
            throw new IllegalArgumentException("Administrators cannot demote their own account to prevent administrative lockout.");
        }

        Role oldRole = target.getRole();
        target.setRole(newRole);
        User saved = userRepository.save(target);

        auditService.logEvent(
                callerEmail,
                "USER_ROLE_UPDATED",
                "USER",
                userId.toString(),
                "SUCCESS",
                String.format("Changed role for %s from %s to %s", target.getEmail(), oldRole, newRole),
                null
        );

        return UserResponse.fromEntity(saved);
    }

    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean active, String callerEmail) {
        User admin = verifyAdminAccess(callerEmail);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Self-protection business rule: An admin cannot deactivate their own active session
        if (target.getId().equals(admin.getId()) && !active) {
            throw new IllegalArgumentException("Administrators cannot deactivate their own active account.");
        }

        target.setActive(active);
        User saved = userRepository.save(target);

        auditService.logEvent(
                callerEmail,
                "USER_STATUS_UPDATED",
                "USER",
                userId.toString(),
                "SUCCESS",
                String.format("User %s active state set to %s", target.getEmail(), active),
                null
        );

        return UserResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteUser(Long userId, String callerEmail) {
        User admin = verifyAdminAccess(callerEmail);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Self-protection rule
        if (target.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("Administrators cannot delete their own active account.");
        }

        userRepository.delete(target);
        auditService.logEvent(
                callerEmail,
                "USER_DELETED",
                "USER",
                userId.toString(),
                "SUCCESS",
                "Deleted user account: " + target.getEmail(),
                null
        );
    }

    // =========================================================================
    // JOB OVERSIGHT
    // =========================================================================

    @Transactional(readOnly = true)
    public Page<Job> listAllJobs(Pageable pageable, String callerEmail) {
        verifyAdminAccess(callerEmail);
        return jobRepository.findAll(pageable);
    }

    @Transactional
    public Job updateJobStatus(Long jobId, String newStatus, String callerEmail) {
        verifyAdminAccess(callerEmail);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        String oldStatus = job.getStatus();
        job.setStatus(newStatus.toUpperCase().trim());
        Job saved = jobRepository.save(job);

        auditService.logEvent(
                callerEmail,
                "JOB_STATUS_UPDATED",
                "JOB",
                jobId.toString(),
                "SUCCESS",
                String.format("Job '%s' status changed from %s to %s", job.getTitle(), oldStatus, job.getStatus()),
                null
        );

        return saved;
    }

    // =========================================================================
    // REPORTS
    // =========================================================================

    @Transactional(readOnly = true)
    public Map<String, Object> getRecruitmentReport(String callerEmail) {
        verifyAdminAccess(callerEmail);

        Map<String, Object> report = new HashMap<>();
        report.put("totalUsers", userRepository.count());
        report.put("totalJobs", jobRepository.count());
        report.put("generatedAt", java.time.LocalDateTime.now());
        report.put("generatedBy", callerEmail);

        return report;
    }

    // =========================================================================
    // SECURITY & ACCESS VERIFICATION
    // =========================================================================

    public User verifyAdminAccess(String callerEmail) {
        if (callerEmail == null || callerEmail.isBlank()) {
            auditService.logEvent("ANONYMOUS", "UNAUTHENTICATED_ADMIN_ACCESS", "SECURITY", null, "FAILURE",
                    "Unauthenticated access attempt to administrative endpoint", null);
            throw new AccessDeniedException("Authentication required.");
        }

        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + callerEmail));

        if (user.getRole() != Role.ADMIN) {
            auditService.logEvent(callerEmail, "UNAUTHORIZED_ADMIN_ACCESS_ATTEMPT", "SECURITY", null, "FAILURE",
                    String.format("User %s with role %s attempted to access administrative resource", callerEmail, user.getRole()),
                    null);
            throw new AccessDeniedException("Access denied: Administrator privileges required.");
        }

        return user;
    }
}
