package com.recruitment.platform.service;

import com.recruitment.platform.model.PlatformConfig;
import com.recruitment.platform.repository.PlatformConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class PlatformConfigService {

    private final PlatformConfigRepository configRepository;
    private final AuditService auditService;

    public PlatformConfigService(PlatformConfigRepository configRepository, AuditService auditService) {
        this.configRepository = configRepository;
        this.auditService = auditService;
    }

    @PostConstruct
    public void initDefaultConfigs() {
        seedIfMissing("AI_AUTO_SHORTLIST_THRESHOLD", "75.0", "Minimum AI match score required to auto-shortlist applicants", "AI");
        seedIfMissing("MAX_RESUME_UPLOAD_SIZE_MB", "10", "Maximum allowed resume document size in Megabytes", "SYSTEM");
        seedIfMissing("MAINTENANCE_MODE", "false", "Flag to toggle platform maintenance window", "SYSTEM");
        seedIfMissing("DEFAULT_INTERVIEW_DURATION_MINUTES", "60", "Default allocated slot time for technical and HR interviews", "RECRUITMENT");
    }

    private void seedIfMissing(String key, String defaultValue, String description, String category) {
        if (!configRepository.existsByConfigKey(key)) {
            configRepository.save(new PlatformConfig(key, defaultValue, description, category, "SYSTEM_INIT"));
        }
    }

    @Transactional(readOnly = true)
    public String getConfig(String key, String defaultValue) {
        return configRepository.findByConfigKey(key)
                .map(PlatformConfig::getConfigValue)
                .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public List<PlatformConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    @Transactional
    public PlatformConfig updateConfig(String key, String value, String actorEmail) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Config key cannot be empty.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Config value cannot be null.");
        }

        PlatformConfig config = configRepository.findByConfigKey(key)
                .orElseGet(() -> new PlatformConfig(key, value, "Custom dynamic configuration", "CUSTOM", actorEmail));

        String oldValue = config.getConfigValue();
        config.setConfigValue(value);
        config.setUpdatedBy(actorEmail);

        PlatformConfig saved = configRepository.save(config);
        auditService.logEvent(
                actorEmail,
                "CONFIG_UPDATED",
                "PLATFORM_CONFIG",
                key,
                "SUCCESS",
                String.format("Updated key '%s' from '%s' to '%s'", key, oldValue, value),
                null
        );
        return saved;
    }
}
