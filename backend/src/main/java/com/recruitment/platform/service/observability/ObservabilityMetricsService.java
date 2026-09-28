package com.recruitment.platform.service.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Enterprise Observability Metrics Service.
 * Manages custom Micrometer metrics for AI operations, pipeline transitions, and system errors.
 */
@Service
public class ObservabilityMetricsService {

    private final MeterRegistry meterRegistry;

    public ObservabilityMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Times an AI operation and increments success/failure counters.
     */
    public <T> T recordAiOperation(String operationName, Callable<T> operation) throws Exception {
        Timer timer = meterRegistry.timer("recruitment.ai.latency", "operation", operationName);
        meterRegistry.counter("recruitment.ai.requests.total", "operation", operationName).increment();
        long start = System.nanoTime();
        try {
            T result = operation.call();
            timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            return result;
        } catch (Exception e) {
            timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            recordAiFailure(operationName, e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Records an AI failure with root cause.
     */
    public void recordAiFailure(String operationName, String failureReason) {
        meterRegistry.counter("recruitment.ai.failures.total",
                "operation", operationName,
                "reason", failureReason != null ? failureReason : "UNKNOWN"
        ).increment();
    }

    /**
     * Records recruitment pipeline stage changes.
     */
    public void recordPipelineTransition(String fromStatus, String toStatus) {
        meterRegistry.counter("recruitment.pipeline.transitions.total",
                "from", fromStatus,
                "to", toStatus
        ).increment();
    }

    /**
     * Records interview lifecycle events.
     */
    public void recordInterviewScheduled(String interviewType) {
        meterRegistry.counter("recruitment.interview.scheduled.total",
                "type", interviewType
        ).increment();
    }

    /**
     * Returns the underlying MeterRegistry for inspection in tests.
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
