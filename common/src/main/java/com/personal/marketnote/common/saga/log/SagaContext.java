package com.personal.marketnote.common.saga.log;

/**
 * SAGA 단계별 진행 상태를 식별하는 불변 VO.
 *
 * <p>SagaInstance 의 sagaId 는 도메인별 정책 (예: {@code ORDER_CANCEL:{orderId}}) 으로 발행되므로
 * 본 VO 는 외부에서 sagaId 를 받아 복원하는 {@link #restore(String, String, String)} 만 노출한다.
 * 신규 UUID 발행이 필요해지면 정적 팩토리를 추가한다.
 */
public record SagaContext(String sagaId, String sagaType, String sagaStep) {

    public static SagaContext restore(String sagaId, String sagaType, String sagaStep) {
        return new SagaContext(sagaId, sagaType, sagaStep);
    }
}
