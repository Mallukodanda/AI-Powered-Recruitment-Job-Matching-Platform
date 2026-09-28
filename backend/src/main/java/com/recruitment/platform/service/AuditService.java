package com.recruitment.platform.service;

import com.recruitment.platform.model.AuditLog;
import com.recruitment.platform.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@SuppressWarnings("null")
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(\"?)(password|passwd|token|secret|apiKey|api_key|authorization|bearer|credential)(\"?\\s*[:=]\\s*\"?)([^\"\\s,}]+)(\"?)",
            Pattern.CASE_INSENSITIVE
    );

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Records an audit event with automatic sensitive data redaction.
     * Uses REQUIRES_NEW propagation so the audit log persists even if the calling transaction fails.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logEvent(String actorEmail, String action, String resourceType,
                             String resourceId, String result, String metadata, String ipAddress) {
        try {
            String sanitizedMetadata = sanitizeMetadata(metadata);
            AuditLog auditLog = new AuditLog(
                    actorEmail,
                    action,
                    resourceType,
                    resourceId,
                    result,
                    sanitizedMetadata,
                    ipAddress
            );
            AuditLog saved = auditLogRepository.save(auditLog);
            log.info("Audit logged [id={}]: actor={}, action={}, resource={}:{}, result={}",
                    saved.getId(), actorEmail, action, resourceType, resourceId, result);
            return saved;
        } catch (Exception ex) {
            log.error("Failed to persist audit log: {}", ex.getMessage(), ex);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(String actor, String action, String resourceType, Pageable pageable) {
        String actorFilter = (actor != null && !actor.isBlank()) ? actor.trim() : null;
        String actionFilter = (action != null && !action.isBlank()) ? action.trim() : null;
        String resourceFilter = (resourceType != null && !resourceType.isBlank()) ? resourceType.trim() : null;

        return auditLogRepository.searchAuditLogs(actorFilter, actionFilter, resourceFilter, pageable);
    }

    /**
     * Cleans sensitive fields such as passwords, tokens, API keys, and authorization secrets.
     */
    public String sanitizeMetadata(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        Matcher matcher = SENSITIVE_PATTERN.matcher(raw);
        return matcher.replaceAll("$1$2$3[REDACTED]$5");
    }
}
