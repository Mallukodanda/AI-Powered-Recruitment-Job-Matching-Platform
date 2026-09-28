package com.recruitment.platform.service;

import com.recruitment.platform.dto.CandidateRankDto;
import com.recruitment.platform.dto.ExplainableMatchResult;
import com.recruitment.platform.dto.MatchScoreResponse;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.Job;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiJobMatchingService {

    private final HybridJobMatchingService hybridJobMatchingService;

    public AiJobMatchingService(HybridJobMatchingService hybridJobMatchingService) {
        this.hybridJobMatchingService = hybridJobMatchingService;
    }

    /**
     * Compute multi-factor hybrid match score between a Job and a Candidate
     */
    public MatchScoreResponse calculateMatch(Job job, Candidate candidate) {
        return hybridJobMatchingService.calculateMatch(job, candidate);
    }

    /**
     * Compute detailed explainable match result
     */
    public ExplainableMatchResult calculateExplainableMatch(Job job, Candidate candidate) {
        return hybridJobMatchingService.match(job, candidate);
    }

    /**
     * Rank an entire pool of candidates for a specific job
     */
    public List<CandidateRankDto> rankCandidatesForJob(Job job, List<Candidate> candidates) {
        List<CandidateRankDto> rankedList = new ArrayList<>();

        for (Candidate candidate : candidates) {
            MatchScoreResponse match = calculateMatch(job, candidate);
            rankedList.add(new CandidateRankDto(0, candidate, match));
        }

        rankedList.sort((a, b) -> Double.compare(
                b.getMatchDetails().getOverallScore(),
                a.getMatchDetails().getOverallScore()));

        for (int i = 0; i < rankedList.size(); i++) {
            rankedList.get(i).setRank(i + 1);
        }

        return rankedList;
    }
}
