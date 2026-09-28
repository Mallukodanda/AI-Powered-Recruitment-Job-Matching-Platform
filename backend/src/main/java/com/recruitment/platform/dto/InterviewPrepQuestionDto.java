package com.recruitment.platform.dto;

import java.util.ArrayList;
import java.util.List;

public class InterviewPrepQuestionDto {

    private String id;
    private String category;
    private String question;
    private String targetSkillOrTopic;
    private String difficulty;
    private List<String> expectedConcepts = new ArrayList<>();
    private String scoringCriteria;
    private String rationale;

    public InterviewPrepQuestionDto() {}

    public InterviewPrepQuestionDto(String id, String category, String question,
                                    String targetSkillOrTopic, String difficulty,
                                    List<String> expectedConcepts, String scoringCriteria,
                                    String rationale) {
        this.id = id;
        this.category = category;
        this.question = question;
        this.targetSkillOrTopic = targetSkillOrTopic;
        this.difficulty = difficulty;
        this.expectedConcepts = expectedConcepts != null ? expectedConcepts : new ArrayList<>();
        this.scoringCriteria = scoringCriteria;
        this.rationale = rationale;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getTargetSkillOrTopic() { return targetSkillOrTopic; }
    public void setTargetSkillOrTopic(String targetSkillOrTopic) { this.targetSkillOrTopic = targetSkillOrTopic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public List<String> getExpectedConcepts() { return expectedConcepts; }
    public void setExpectedConcepts(List<String> expectedConcepts) { this.expectedConcepts = expectedConcepts; }

    public String getScoringCriteria() { return scoringCriteria; }
    public void setScoringCriteria(String scoringCriteria) { this.scoringCriteria = scoringCriteria; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
}
