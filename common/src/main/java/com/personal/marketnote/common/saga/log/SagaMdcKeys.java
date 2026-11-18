package com.personal.marketnote.common.saga.log;

/**
 * SAGA 단계별 진행 상태를 MDC 에 enrich 할 때 사용하는 키 모음.
 *
 * <p>{@link SagaStepLogger} 가 SAGA 시작/완료/실패/보상 시점에 본 상수의 키를 사용하여
 * MDC put/remove 한다. logback pattern 에 키를 추가하면 자동으로 출력된다.
 */
public final class SagaMdcKeys {

    public static final String SAGA_ID = "sagaId";
    public static final String SAGA_TYPE = "sagaType";
    public static final String SAGA_STEP = "sagaStep";
    public static final String SAGA_COMPENSATION_FAILURE = "sagaCompensationFailure";

    private SagaMdcKeys() {
    }
}
