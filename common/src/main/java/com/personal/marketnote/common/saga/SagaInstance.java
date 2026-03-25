package com.personal.marketnote.common.saga;

import com.personal.marketnote.common.saga.exception.InvalidSagaStatusTransitionException;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.Clock;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SagaInstance {

    private Long id;
    private String sagaId;
    private String sagaType;
    private SagaStatus status;
    private int currentStepIndex;
    private String payload;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime completedAt;

    public static SagaInstance from(SagaInstanceCreateState state, Clock clock) {
        if (FormatValidator.hasNoValue(state.sagaId())) {
            throw new IllegalArgumentException("sagaId는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.sagaType())) {
            throw new IllegalArgumentException("sagaType은 필수입니다.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return SagaInstance.builder()
                .sagaId(state.sagaId())
                .sagaType(state.sagaType())
                .status(SagaStatus.STARTED)
                .currentStepIndex(0)
                .payload(state.payload())
                .createdAt(now)
                .modifiedAt(now)
                .build();
    }

    public static SagaInstance from(SagaInstanceSnapshotState state) {
        return SagaInstance.builder()
                .id(state.id())
                .sagaId(state.sagaId())
                .sagaType(state.sagaType())
                .status(state.status())
                .currentStepIndex(state.currentStepIndex())
                .payload(state.payload())
                .createdAt(state.createdAt())
                .modifiedAt(state.modifiedAt())
                .completedAt(state.completedAt())
                .build();
    }

    public void process() {
        if (!status.canProcess()) {
            throw new InvalidSagaStatusTransitionException(status);
        }
        this.status = SagaStatus.PROCESSING;
    }

    public void advanceStep() {
        if (!status.isProcessing()) {
            throw new InvalidSagaStatusTransitionException(status);
        }
        this.currentStepIndex++;
    }

    public void complete(Clock clock) {
        if (!status.isProcessing()) {
            throw new InvalidSagaStatusTransitionException(status);
        }
        this.status = SagaStatus.SUCCEEDED;
        this.completedAt = LocalDateTime.now(clock);
    }

    public void fail() {
        if (!status.isProcessing()) {
            throw new InvalidSagaStatusTransitionException(status);
        }
        this.status = SagaStatus.FAILED;
    }

    public void rollback() {
        if (!status.canCompensate()) {
            throw new InvalidSagaStatusTransitionException(status);
        }
        this.status = SagaStatus.COMPENSATING;
    }

    public void compensate(Clock clock) {
        if (!status.isCompensating()) {
            throw new InvalidSagaStatusTransitionException(status);
        }
        this.status = SagaStatus.COMPENSATED;
        this.completedAt = LocalDateTime.now(clock);
    }

    public void failCompensation() {
        if (!status.isCompensating()) {
            throw new InvalidSagaStatusTransitionException(status);
        }
        this.status = SagaStatus.FAILED;
    }

    public boolean isTerminal() {
        return status.isTerminal();
    }
}
