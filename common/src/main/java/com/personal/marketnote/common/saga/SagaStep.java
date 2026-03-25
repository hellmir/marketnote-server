package com.personal.marketnote.common.saga;

import com.personal.marketnote.common.saga.exception.*;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.Clock;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SagaStep {

    private Long id;
    private Long sagaInstanceId;
    private String stepName;
    private int stepIndex;
    private SagaStepStatus status;
    private String request;
    private String response;
    private String compensationRequest;
    private String compensationResponse;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static SagaStep from(SagaStepCreateState state, Clock clock) {
        if (FormatValidator.hasNoValue(state.sagaInstanceId())) {
            throw new SagaInstanceIdNoValueException();
        }
        if (FormatValidator.hasNoValue(state.stepName())) {
            throw new SagaStepNameNoValueException();
        }
        if (FormatValidator.hasNoValue(state.request())) {
            throw new SagaStepRequestNoValueException();
        }
        if (state.stepIndex() < 0) {
            throw new InvalidSagaStepIndexException(state.stepIndex());
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return SagaStep.builder()
                .sagaInstanceId(state.sagaInstanceId())
                .stepName(state.stepName())
                .stepIndex(state.stepIndex())
                .status(SagaStepStatus.PENDING)
                .request(state.request())
                .createdAt(now)
                .modifiedAt(now)
                .build();
    }

    public static SagaStep from(SagaStepSnapshotState state) {
        return SagaStep.builder()
                .id(state.id())
                .sagaInstanceId(state.sagaInstanceId())
                .stepName(state.stepName())
                .stepIndex(state.stepIndex())
                .status(state.status())
                .request(state.request())
                .response(state.response())
                .compensationRequest(state.compensationRequest())
                .compensationResponse(state.compensationResponse())
                .createdAt(state.createdAt())
                .modifiedAt(state.modifiedAt())
                .build();
    }

    public void process() {
        if (!status.canProcess()) {
            throw new InvalidSagaStepStatusTransitionException(status);
        }
        this.status = SagaStepStatus.PROCESSING;
    }

    public void succeed(String response) {
        if (!status.isProcessing()) {
            throw new InvalidSagaStepStatusTransitionException(status);
        }
        this.status = SagaStepStatus.SUCCEEDED;
        this.response = response;
    }

    public void fail(String response) {
        if (!status.isProcessing()) {
            throw new InvalidSagaStepStatusTransitionException(status);
        }
        this.status = SagaStepStatus.FAILED;
        this.response = response;
    }

    public void compensate(String compensationRequest) {
        if (FormatValidator.hasNoValue(compensationRequest)) {
            throw new SagaStepCompensationRequestNoValueException();
        }
        if (!status.canCompensate()) {
            throw new InvalidSagaStepStatusTransitionException(status);
        }
        this.status = SagaStepStatus.COMPENSATING;
        this.compensationRequest = compensationRequest;
    }

    public void completeCompensation(String compensationResponse) {
        if (!status.isCompensating()) {
            throw new InvalidSagaStepStatusTransitionException(status);
        }
        this.status = SagaStepStatus.COMPENSATED;
        this.compensationResponse = compensationResponse;
    }

    public void failCompensation(String compensationResponse) {
        if (!status.isCompensating()) {
            throw new InvalidSagaStepStatusTransitionException(status);
        }
        this.status = SagaStepStatus.FAILED;
        this.compensationResponse = compensationResponse;
    }

    public boolean isPending() {
        return status.isPending();
    }

    public boolean isProcessing() {
        return status.isProcessing();
    }

    public boolean isSucceeded() {
        return status.isSucceeded();
    }

    public boolean isFailed() {
        return status.isFailed();
    }

    public boolean isCompensating() {
        return status.isCompensating();
    }

    public boolean isCompensated() {
        return status.isCompensated();
    }

    public boolean isTerminal() {
        return status.isTerminal();
    }

    public boolean requiresCompensation() {
        return status.isSucceeded();
    }
}
