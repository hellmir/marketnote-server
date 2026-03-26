package com.personal.marketnote.common.saga;

import com.personal.marketnote.common.saga.port.FindSagaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SagaTimeoutChecker 테스트")
class SagaTimeoutCheckerTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-03-16T01:00:00Z");
    private static final LocalDateTime FIXED_TIME = LocalDateTime.ofInstant(FIXED_INSTANT, ZONE);
    private static final LocalDateTime PROCESSING_CUTOFF = FIXED_TIME.minus(60000L, ChronoUnit.MILLIS);
    private static final LocalDateTime COMPENSATION_CUTOFF = FIXED_TIME.minus(120000L, ChronoUnit.MILLIS);
    private static final String SAGA_ID_1 = "saga-001";
    private static final String SAGA_ID_2 = "saga-002";
    private static final String SAGA_ID_3 = "saga-003";
    private static final String SAGA_TYPE = "ORDER_PAYMENT";
    private static final long TIMEOUT_MS = 60000L;
    private static final long COMPENSATION_TIMEOUT_MS = 120000L;

    @Mock
    private FindSagaPort findSagaPort;

    @Mock
    private SagaOrchestrator sagaOrchestrator;

    private SagaTimeoutChecker sagaTimeoutChecker;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_INSTANT, ZONE);
        SagaProperties sagaProperties = new SagaProperties();
        sagaProperties.setTimeoutMs(TIMEOUT_MS);
        sagaProperties.setCompensationTimeoutMs(COMPENSATION_TIMEOUT_MS);
        sagaTimeoutChecker = new SagaTimeoutChecker(findSagaPort, sagaOrchestrator, sagaProperties, clock);
    }

    @Nested
    @DisplayName("checkProcessingTimeouts")
    class CheckProcessingTimeouts {

        @Test
        @DisplayName("PROCESSING 타임아웃된 인스턴스가 있으면 handleProcessingTimeout이 호출된다")
        void shouldCallHandleProcessingTimeoutWhenTimedOutInstanceExists() {
            // given
            SagaInstance instance = createInstance(SAGA_ID_1, SagaStatus.PROCESSING);
            when(findSagaPort.findTimedOutProcessingInstances(PROCESSING_CUTOFF))
                    .thenReturn(List.of(instance));
            when(findSagaPort.findTimedOutCompensatingInstances(COMPENSATION_CUTOFF))
                    .thenReturn(Collections.emptyList());

            // when
            sagaTimeoutChecker.checkTimeouts();

            // then
            verify(sagaOrchestrator).handleProcessingTimeout(SAGA_ID_1);
            verifyNoMoreInteractions(sagaOrchestrator);
        }

        @Test
        @DisplayName("PROCESSING 타임아웃된 인스턴스가 여러 건이면 각각 handleProcessingTimeout이 호출된다")
        void shouldCallHandleProcessingTimeoutForEachTimedOutInstance() {
            // given
            SagaInstance instance1 = createInstance(SAGA_ID_1, SagaStatus.PROCESSING);
            SagaInstance instance2 = createInstance(SAGA_ID_2, SagaStatus.PROCESSING);
            SagaInstance instance3 = createInstance(SAGA_ID_3, SagaStatus.PROCESSING);
            when(findSagaPort.findTimedOutProcessingInstances(PROCESSING_CUTOFF))
                    .thenReturn(List.of(instance1, instance2, instance3));
            when(findSagaPort.findTimedOutCompensatingInstances(COMPENSATION_CUTOFF))
                    .thenReturn(Collections.emptyList());

            // when
            sagaTimeoutChecker.checkTimeouts();

            // then
            verify(sagaOrchestrator).handleProcessingTimeout(SAGA_ID_1);
            verify(sagaOrchestrator).handleProcessingTimeout(SAGA_ID_2);
            verify(sagaOrchestrator).handleProcessingTimeout(SAGA_ID_3);
            verifyNoMoreInteractions(sagaOrchestrator);
        }

        @Test
        @DisplayName("PROCESSING 타임아웃된 인스턴스가 없으면 handleProcessingTimeout이 호출되지 않는다")
        void shouldNotCallHandleProcessingTimeoutWhenNoTimedOutInstances() {
            // given
            when(findSagaPort.findTimedOutProcessingInstances(PROCESSING_CUTOFF))
                    .thenReturn(Collections.emptyList());
            when(findSagaPort.findTimedOutCompensatingInstances(COMPENSATION_CUTOFF))
                    .thenReturn(Collections.emptyList());

            // when
            sagaTimeoutChecker.checkTimeouts();

            // then
            verify(sagaOrchestrator, never()).handleProcessingTimeout(any());
            verify(sagaOrchestrator, never()).handleCompensationTimeout(any());
        }

        @Test
        @DisplayName("PROCESSING 타임아웃 처리 중 한 인스턴스에서 예외가 발생해도 나머지 인스턴스 처리는 계속된다")
        void shouldContinueProcessingWhenOneInstanceThrowsException() {
            // given
            SagaInstance instance1 = createInstance(SAGA_ID_1, SagaStatus.PROCESSING);
            SagaInstance instance2 = createInstance(SAGA_ID_2, SagaStatus.PROCESSING);
            SagaInstance instance3 = createInstance(SAGA_ID_3, SagaStatus.PROCESSING);
            when(findSagaPort.findTimedOutProcessingInstances(PROCESSING_CUTOFF))
                    .thenReturn(List.of(instance1, instance2, instance3));
            when(findSagaPort.findTimedOutCompensatingInstances(COMPENSATION_CUTOFF))
                    .thenReturn(Collections.emptyList());
            doThrow(new RuntimeException("처리 실패")).when(sagaOrchestrator).handleProcessingTimeout(SAGA_ID_2);

            // when
            sagaTimeoutChecker.checkTimeouts();

            // then
            verify(sagaOrchestrator).handleProcessingTimeout(SAGA_ID_1);
            verify(sagaOrchestrator).handleProcessingTimeout(SAGA_ID_2);
            verify(sagaOrchestrator).handleProcessingTimeout(SAGA_ID_3);
        }
    }

    @Nested
    @DisplayName("checkCompensatingTimeouts")
    class CheckCompensatingTimeouts {

        @Test
        @DisplayName("COMPENSATING 타임아웃된 인스턴스가 있으면 handleCompensationTimeout이 호출된다")
        void shouldCallHandleCompensationTimeoutWhenTimedOutInstanceExists() {
            // given
            SagaInstance instance = createInstance(SAGA_ID_1, SagaStatus.COMPENSATING);
            when(findSagaPort.findTimedOutProcessingInstances(PROCESSING_CUTOFF))
                    .thenReturn(Collections.emptyList());
            when(findSagaPort.findTimedOutCompensatingInstances(COMPENSATION_CUTOFF))
                    .thenReturn(List.of(instance));

            // when
            sagaTimeoutChecker.checkTimeouts();

            // then
            verify(sagaOrchestrator).handleCompensationTimeout(SAGA_ID_1);
            verifyNoMoreInteractions(sagaOrchestrator);
        }

        @Test
        @DisplayName("COMPENSATING 타임아웃된 인스턴스가 여러 건이면 각각 handleCompensationTimeout이 호출된다")
        void shouldCallHandleCompensationTimeoutForEachTimedOutInstance() {
            // given
            SagaInstance instance1 = createInstance(SAGA_ID_1, SagaStatus.COMPENSATING);
            SagaInstance instance2 = createInstance(SAGA_ID_2, SagaStatus.COMPENSATING);
            SagaInstance instance3 = createInstance(SAGA_ID_3, SagaStatus.COMPENSATING);
            when(findSagaPort.findTimedOutProcessingInstances(PROCESSING_CUTOFF))
                    .thenReturn(Collections.emptyList());
            when(findSagaPort.findTimedOutCompensatingInstances(COMPENSATION_CUTOFF))
                    .thenReturn(List.of(instance1, instance2, instance3));

            // when
            sagaTimeoutChecker.checkTimeouts();

            // then
            verify(sagaOrchestrator).handleCompensationTimeout(SAGA_ID_1);
            verify(sagaOrchestrator).handleCompensationTimeout(SAGA_ID_2);
            verify(sagaOrchestrator).handleCompensationTimeout(SAGA_ID_3);
            verifyNoMoreInteractions(sagaOrchestrator);
        }

        @Test
        @DisplayName("COMPENSATING 타임아웃된 인스턴스가 없으면 handleCompensationTimeout이 호출되지 않는다")
        void shouldNotCallHandleCompensationTimeoutWhenNoTimedOutInstances() {
            // given
            when(findSagaPort.findTimedOutProcessingInstances(PROCESSING_CUTOFF))
                    .thenReturn(Collections.emptyList());
            when(findSagaPort.findTimedOutCompensatingInstances(COMPENSATION_CUTOFF))
                    .thenReturn(Collections.emptyList());

            // when
            sagaTimeoutChecker.checkTimeouts();

            // then
            verify(sagaOrchestrator, never()).handleCompensationTimeout(any());
            verify(sagaOrchestrator, never()).handleProcessingTimeout(any());
        }

        @Test
        @DisplayName("COMPENSATING 타임아웃 처리 중 한 인스턴스에서 예외가 발생해도 나머지 인스턴스 처리는 계속된다")
        void shouldContinueCompensatingWhenOneInstanceThrowsException() {
            // given
            SagaInstance instance1 = createInstance(SAGA_ID_1, SagaStatus.COMPENSATING);
            SagaInstance instance2 = createInstance(SAGA_ID_2, SagaStatus.COMPENSATING);
            SagaInstance instance3 = createInstance(SAGA_ID_3, SagaStatus.COMPENSATING);
            when(findSagaPort.findTimedOutProcessingInstances(PROCESSING_CUTOFF))
                    .thenReturn(Collections.emptyList());
            when(findSagaPort.findTimedOutCompensatingInstances(COMPENSATION_CUTOFF))
                    .thenReturn(List.of(instance1, instance2, instance3));
            doThrow(new RuntimeException("보상 처리 실패")).when(sagaOrchestrator).handleCompensationTimeout(SAGA_ID_2);

            // when
            sagaTimeoutChecker.checkTimeouts();

            // then
            verify(sagaOrchestrator).handleCompensationTimeout(SAGA_ID_1);
            verify(sagaOrchestrator).handleCompensationTimeout(SAGA_ID_2);
            verify(sagaOrchestrator).handleCompensationTimeout(SAGA_ID_3);
        }
    }

    @Nested
    @DisplayName("checkTimeouts 통합")
    class CheckTimeouts {

        @Test
        @DisplayName("checkTimeouts 호출 시 PROCESSING과 COMPENSATING 타임아웃을 모두 확인한다")
        void shouldCheckBothProcessingAndCompensatingTimeouts() {
            // given
            SagaInstance processingInstance = createInstance(SAGA_ID_1, SagaStatus.PROCESSING);
            SagaInstance compensatingInstance = createInstance(SAGA_ID_2, SagaStatus.COMPENSATING);
            when(findSagaPort.findTimedOutProcessingInstances(PROCESSING_CUTOFF))
                    .thenReturn(List.of(processingInstance));
            when(findSagaPort.findTimedOutCompensatingInstances(COMPENSATION_CUTOFF))
                    .thenReturn(List.of(compensatingInstance));

            // when
            sagaTimeoutChecker.checkTimeouts();

            // then
            verify(sagaOrchestrator).handleProcessingTimeout(SAGA_ID_1);
            verify(sagaOrchestrator).handleCompensationTimeout(SAGA_ID_2);
            verifyNoMoreInteractions(sagaOrchestrator);
        }
    }

    // --- Helper Methods ---

    private SagaInstance createInstance(String sagaId, SagaStatus status) {
        return SagaInstance.from(new SagaInstanceSnapshotState(
                1L, sagaId, SAGA_TYPE, status, 0, "{}",
                FIXED_TIME, FIXED_TIME, null
        ));
    }
}
