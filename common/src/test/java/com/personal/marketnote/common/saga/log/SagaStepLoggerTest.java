package com.personal.marketnote.common.saga.log;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SagaStepLoggerTest {

    private SagaContext context;

    @BeforeEach
    void setUp() {
        MDC.clear();
        context = SagaContext.restore("saga-1", "ORDER_CANCEL", "STEP_A");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("start()는 sagaId/sagaType/sagaStep을 MDC에 put 한다")
    void shouldPutMdcKeysOnStart() {
        SagaStepLogger.start(context);

        assertThat(MDC.get(SagaMdcKeys.SAGA_ID)).isEqualTo("saga-1");
        assertThat(MDC.get(SagaMdcKeys.SAGA_TYPE)).isEqualTo("ORDER_CANCEL");
        assertThat(MDC.get(SagaMdcKeys.SAGA_STEP)).isEqualTo("STEP_A");
    }

    @Test
    @DisplayName("complete()는 SAGA MDC 키를 제거한다")
    void shouldClearMdcKeysOnComplete() {
        SagaStepLogger.start(context);

        SagaStepLogger.complete(context);

        assertThat(MDC.get(SagaMdcKeys.SAGA_ID)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_TYPE)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_STEP)).isNull();
    }

    @Test
    @DisplayName("fail()은 SAGA MDC 키를 제거한다")
    void shouldClearMdcKeysOnFail() {
        SagaStepLogger.start(context);

        SagaStepLogger.fail(context, new RuntimeException("boom"));

        assertThat(MDC.get(SagaMdcKeys.SAGA_ID)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_TYPE)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_STEP)).isNull();
    }

    @Test
    @DisplayName("compensateFailed()는 ERROR 로깅 후 모든 SAGA MDC 키를 정리한다")
    void shouldClearAllSagaMdcKeysOnCompensationFailure() {
        SagaStepLogger.start(context);

        SagaStepLogger.compensateFailed(context, new RuntimeException("compensation boom"));

        assertThat(MDC.get(SagaMdcKeys.SAGA_ID)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_TYPE)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_STEP)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_COMPENSATION_FAILURE)).isNull();
    }

    @Test
    @DisplayName("compensate()는 sagaId/sagaType/sagaStep을 MDC에 put 한다")
    void shouldPutMdcKeysOnCompensate() {
        SagaStepLogger.compensate(context);

        assertThat(MDC.get(SagaMdcKeys.SAGA_ID)).isEqualTo("saga-1");
        assertThat(MDC.get(SagaMdcKeys.SAGA_TYPE)).isEqualTo("ORDER_CANCEL");
        assertThat(MDC.get(SagaMdcKeys.SAGA_STEP)).isEqualTo("STEP_A");
    }

    @Test
    @DisplayName("runWithMdc()는 정상 실행 시 결과를 반환하고 MDC를 정리한다")
    void shouldReturnResultAndClearMdcOnSuccess() {
        String result = SagaStepLogger.runWithMdc(context, () -> {
            assertThat(MDC.get(SagaMdcKeys.SAGA_ID)).isEqualTo("saga-1");
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(MDC.get(SagaMdcKeys.SAGA_ID)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_TYPE)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_STEP)).isNull();
    }

    @Test
    @DisplayName("runWithMdc()는 예외 발생 시 rethrow 하고 MDC를 정리한다")
    void shouldRethrowAndClearMdcOnException() {
        assertThatThrownBy(() -> SagaStepLogger.runWithMdc(context, () -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class)
            .hasMessage("boom");

        assertThat(MDC.get(SagaMdcKeys.SAGA_ID)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_TYPE)).isNull();
        assertThat(MDC.get(SagaMdcKeys.SAGA_STEP)).isNull();
    }

    @Test
    @DisplayName("runWithMdc()는 외부 MDC 값을 보존하지 않는다 (SAGA 키만 정리)")
    void shouldOnlyClearSagaKeys() {
        MDC.put("traceId", "trace-1");

        SagaStepLogger.runWithMdc(context, () -> "ok");

        assertThat(MDC.get("traceId")).isEqualTo("trace-1");
        assertThat(MDC.get(SagaMdcKeys.SAGA_ID)).isNull();
    }
}
