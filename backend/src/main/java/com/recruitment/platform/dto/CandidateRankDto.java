package com.recruitment.platform.dto;

import com.recruitment.platform.model.Candidate;

public class CandidateRankDto {

    private int rank;
    private Candidate candidate;
    private MatchScoreResponse matchDetails;

    public CandidateRankDto() {}

    public CandidateRankDto(int rank, Candidate candidate, MatchScoreResponse matchDetails) {
        this.rank = rank;
        this.candidate = candidate;
        this.matchDetails = matchDetails;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public MatchScoreResponse getMatchDetails() { return matchDetails; }
    public void setMatchDetails(MatchScoreResponse matchDetails) { this.matchDetails = matchDetails; }
}
