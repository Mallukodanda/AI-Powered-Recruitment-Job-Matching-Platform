package com.recruitment.platform.dto;

import java.util.ArrayList;
import java.util.List;

public class ResumeParseResult {

    private String extractedName;
    private String extractedEmail;
    private String extractedPhone;
    private String extractedTitle;
    private Integer estimatedExperienceYears;
    private String extractedEducation;
    private List<String> extractedSkills = new ArrayList<>();
    private String rawText;

    public ResumeParseResult() {}

    public String getExtractedName() { return extractedName; }
    public void setExtractedName(String extractedName) { this.extractedName = extractedName; }

    public String getExtractedEmail() { return extractedEmail; }
    public void setExtractedEmail(String extractedEmail) { this.extractedEmail = extractedEmail; }

    public String getExtractedPhone() { return extractedPhone; }
    public void setExtractedPhone(String extractedPhone) { this.extractedPhone = extractedPhone; }

    public String getExtractedTitle() { return extractedTitle; }
    public void setExtractedTitle(String extractedTitle) { this.extractedTitle = extractedTitle; }

    public Integer getEstimatedExperienceYears() { return estimatedExperienceYears; }
    public void setEstimatedExperienceYears(Integer estimatedExperienceYears) { this.estimatedExperienceYears = estimatedExperienceYears; }

    public String getExtractedEducation() { return extractedEducation; }
    public void setExtractedEducation(String extractedEducation) { this.extractedEducation = extractedEducation; }

    public List<String> getExtractedSkills() { return extractedSkills; }
    public void setExtractedSkills(List<String> extractedSkills) { this.extractedSkills = extractedSkills; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
