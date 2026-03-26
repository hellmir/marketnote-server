package com.personal.marketnote.common.saga;

import com.personal.marketnote.common.saga.port.FindSagaPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class SagaTimeoutChecker {

    private final FindSagaPort findSagaPort;
    private final SagaOrchestrator sagaOrchestrator;
    private final SagaProperties sagaProperties;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${saga.check-interval-ms:30000}")
    public void checkTimeouts() {
        checkProcessingTimeouts();
        checkCompensatingTimeouts();
    }

    private void checkProcessingTimeouts() {
        LocalDateTime cutoff = LocalDateTime.now(clock)
                .minus(sagaProperties.getTimeoutMs(), ChronoUnit.MILLIS);
        List<SagaInstance> timedOutInstances = findSagaPort.findTimedOutProcessingInstances(cutoff);

        for (SagaInstance instance : timedOutInstances) {
            handleProcessingTimeoutSafely(instance);
        }
    }

    private void checkCompensatingTimeouts() {
        LocalDateTime cutoff = LocalDateTime.now(clock)
                .minus(sagaProperties.getCompensationTimeoutMs(), ChronoUnit.MILLIS);
        List<SagaInstance> timedOutInstances = findSagaPort.findTimedOutCompensatingInstances(cutoff);

        for (SagaInstance instance : timedOutInstances) {
            handleCompensatingTimeoutSafely(instance);
        }
    }

    private void handleProcessingTimeoutSafely(SagaInstance instance) {
        try {
            log.warn("SAGA PROCESSING 타임아웃 감지. sagaId={}, sagaType={}, modifiedAt={}",
                    instance.getSagaId(), instance.getSagaType(), instance.getModifiedAt());
            sagaOrchestrator.handleProcessingTimeout(instance.getSagaId());
        } catch (Exception e) {
            log.error("SAGA PROCESSING 타임아웃 처리 실패. sagaId={}", instance.getSagaId(), e);
        }
    }

    private void handleCompensatingTimeoutSafely(SagaInstance instance) {
        try {
            log.warn("SAGA COMPENSATING 타임아웃 감지. sagaId={}, sagaType={}, modifiedAt={}",
                    instance.getSagaId(), instance.getSagaType(), instance.getModifiedAt());
            sagaOrchestrator.handleCompensationTimeout(instance.getSagaId());
        } catch (Exception e) {
            log.error("SAGA COMPENSATING 타임아웃 처리 실패. sagaId={}", instance.getSagaId(), e);
        }
    }
}
