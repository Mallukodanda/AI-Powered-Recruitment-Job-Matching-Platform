package com.recruitment.platform.service.ai;

import com.recruitment.platform.dto.StructuredResumeData;

public interface StructuredResumeAnalyzer {
    StructuredResumeData analyzeText(String extractedText);
    String getProviderName();
    String getModelName();
}
