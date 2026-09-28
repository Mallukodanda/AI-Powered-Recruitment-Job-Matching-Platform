package com.recruitment.platform.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Enumeration representing the stages of the candidate hiring pipeline.
 * Encapsulates the recruitment lifecycle state machine and valid stage transitions.
 */
public enum ApplicationStatus {
    APPLIED,
    UNDER_REVIEW,
    AI_SCREENED,
    SHORTLISTED,
    INTERVIEW_SCHEDULED,
    OFFERED,
    HIRED,
    REJECTED;

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = Map.of(
            APPLIED, EnumSet.of(APPLIED, UNDER_REVIEW, AI_SCREENED, SHORTLISTED, REJECTED),
            UNDER_REVIEW, EnumSet.of(UNDER_REVIEW, AI_SCREENED, SHORTLISTED, REJECTED),
            AI_SCREENED, EnumSet.of(AI_SCREENED, UNDER_REVIEW, SHORTLISTED, REJECTED),
            SHORTLISTED, EnumSet.of(SHORTLISTED, UNDER_REVIEW, INTERVIEW_SCHEDULED, REJECTED),
            INTERVIEW_SCHEDULED, EnumSet.of(INTERVIEW_SCHEDULED, SHORTLISTED, OFFERED, REJECTED),
            OFFERED, EnumSet.of(OFFERED, HIRED, REJECTED),
            HIRED, EnumSet.of(HIRED),
            REJECTED, EnumSet.of(REJECTED)
        );
    }

    /**
     * Determines whether transitioning from this status to the target status is permitted.
     */
    public boolean canTransitionTo(ApplicationStatus target) {
        if (target == null) {
            return false;
        }
        Set<ApplicationStatus> allowed = VALID_TRANSITIONS.getOrDefault(this, Collections.emptySet());
        return allowed.contains(target);
    }

    /**
     * Returns true if this state is a terminal milestone where no further progression is allowed.
     */
    public boolean isTerminal() {
        return this == HIRED || this == REJECTED;
    }
}
