package com.recruitment.platform.service.ai;

import com.recruitment.platform.dto.StructuredJobData;

public interface JobDescriptionAnalyzer {
    String getProviderName();
    String getModelName();
    StructuredJobData analyzeJob(String title, String description, String requirements, String rawSkills, Integer minExp, String education);
}
