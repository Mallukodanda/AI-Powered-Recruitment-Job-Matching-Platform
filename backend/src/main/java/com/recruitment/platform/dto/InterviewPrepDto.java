package com.recruitment.platform.dto;

import java.util.ArrayList;
import java.util.List;

public class InterviewPrepDto {

    private Long candidateId;
    private String candidateName;
    private String candidateTitle;
    private Integer yearsExperience;
    private Long jobId;
    private String jobTitle;
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingOrGrowthSkills = new ArrayList<>();
    private List<String> preparationTopics = new ArrayList<>();
    private List<InterviewPrepQuestionDto> technicalQuestions = new ArrayList<>();
    private List<InterviewPrepQuestionDto> roleSpecificQuestions = new ArrayList<>();
    private List<InterviewPrepQuestionDto> hrQuestions = new ArrayList<>();
    private List<InterviewPrepQuestionDto> followUpQuestions = new ArrayList<>();
    private String disclaimer = "ASSISTIVE PREPARATION TOOL ONLY: This AI assistant generates preparation topics and structured question guides to empower human interviewers. It does NOT make automated hiring or rejection decisions.";

    public InterviewPrepDto() {}

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateTitle() { return candidateTitle; }
    public void setCandidateTitle(String candidateTitle) { this.candidateTitle = candidateTitle; }

    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingOrGrowthSkills() { return missingOrGrowthSkills; }
    public void setMissingOrGrowthSkills(List<String> missingOrGrowthSkills) { this.missingOrGrowthSkills = missingOrGrowthSkills; }

    public List<String> getPreparationTopics() { return preparationTopics; }
    public void setPreparationTopics(List<String> preparationTopics) { this.preparationTopics = preparationTopics; }

    public List<InterviewPrepQuestionDto> getTechnicalQuestions() { return technicalQuestions; }
    public void setTechnicalQuestions(List<InterviewPrepQuestionDto> technicalQuestions) { this.technicalQuestions = technicalQuestions; }

    public List<InterviewPrepQuestionDto> getRoleSpecificQuestions() { return roleSpecificQuestions; }
    public void setRoleSpecificQuestions(List<InterviewPrepQuestionDto> roleSpecificQuestions) { this.roleSpecificQuestions = roleSpecificQuestions; }

    public List<InterviewPrepQuestionDto> getHrQuestions() { return hrQuestions; }
    public void setHrQuestions(List<InterviewPrepQuestionDto> hrQuestions) { this.hrQuestions = hrQuestions; }

    public List<InterviewPrepQuestionDto> getFollowUpQuestions() { return followUpQuestions; }
    public void setFollowUpQuestions(List<InterviewPrepQuestionDto> followUpQuestions) { this.followUpQuestions = followUpQuestions; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
