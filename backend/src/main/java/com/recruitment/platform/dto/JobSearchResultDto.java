package com.recruitment.platform.dto;

import com.recruitment.platform.model.Job;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Job search result item with optional semantic similarity score and matching highlights")
public class JobSearchResultDto {

    private Job job;

    @Schema(description = "Dense vector semantic relevance score (0.0 to 100.0) if semantic query was provided")
    private Double semanticRelevanceScore;

    @Schema(description = "Keywords and criteria matched in this result")
    private List<String> matchHighlights;

    public JobSearchResultDto() {}

    public JobSearchResultDto(Job job, Double semanticRelevanceScore, List<String> matchHighlights) {
        this.job = job;
        this.semanticRelevanceScore = semanticRelevanceScore;
        this.matchHighlights = matchHighlights;
    }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public Double getSemanticRelevanceScore() { return semanticRelevanceScore; }
    public void setSemanticRelevanceScore(Double semanticRelevanceScore) { this.semanticRelevanceScore = semanticRelevanceScore; }

    public List<String> getMatchHighlights() { return matchHighlights; }
    public void setMatchHighlights(List<String> matchHighlights) { this.matchHighlights = matchHighlights; }
}
