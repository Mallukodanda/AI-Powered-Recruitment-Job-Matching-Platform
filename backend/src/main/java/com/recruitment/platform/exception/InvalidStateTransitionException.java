package com.recruitment.platform.exception;

import com.recruitment.platform.model.ApplicationStatus;

public class InvalidStateTransitionException extends RuntimeException {

    private final ApplicationStatus fromStatus;
    private final ApplicationStatus toStatus;

    public InvalidStateTransitionException(String message) {
        super(message);
        this.fromStatus = null;
        this.toStatus = null;
    }

    public InvalidStateTransitionException(ApplicationStatus fromStatus, ApplicationStatus toStatus) {
        super(String.format("Invalid pipeline state transition from %s to %s. Candidate must follow valid recruitment pipeline workflow.",
                fromStatus, toStatus));
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public ApplicationStatus getFromStatus() {
        return fromStatus;
    }

    public ApplicationStatus getToStatus() {
        return toStatus;
    }
}
