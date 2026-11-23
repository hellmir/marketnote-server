package com.personal.marketnote.common.utility.audit;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 벤더 통신 이력을 기록하는 공통 유틸리티.
 *
 * <p>서비스별 Recorder 중복을 제거하기 위해 target/vendor/type/sender enum과 dispatcher를 타입 파라미터로 받아
 * 8종 record(...) 오버로드를 제공한다. 서비스별 어댑터는 {@link VendorCommunicationHistoryDispatcher}를
 * 주입하여 자신의 UseCase로 위임한다.</p>
 *
 * <p>오버로드는 두 그룹으로 나뉜다:
 * <ul>
 *   <li>기존 4종(attemptCount 미명시): 내부적으로 {@code attemptCount=1}을 dispatcher에 전달하여 단일 호출 의미를 보존한다.</li>
 *   <li>신규 4종(attemptCount 명시): 재시도 횟수를 명시적으로 전달해야 하는 호출 사이트에서 사용한다.</li>
 * </ul>
 * </p>
 *
 * @param <T> TargetType enum 타입
 * @param <V> VendorName enum 타입
 * @param <C> CommunicationType enum 타입
 * @param <S> SenderType enum 타입
 */
public class VendorCommunicationRecorder<
        T extends Enum<T>,
        V extends Enum<V>,
        C extends Enum<C>,
        S extends Enum<S>
        > {
    private static final int DEFAULT_ATTEMPT_COUNT = 1;

    private final VendorCommunicationHistoryDispatcher<T, V, C, S> dispatcher;

    public VendorCommunicationRecorder(VendorCommunicationHistoryDispatcher<T, V, C, S> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            V vendorName,
            String payload,
            JsonNode payloadJson
    ) {
        dispatcher.dispatch(
                targetType, null, vendorName, communicationType, sender,
                null, payload, payloadJson, DEFAULT_ATTEMPT_COUNT
        );
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            String targetId,
            V vendorName,
            String payload,
            JsonNode payloadJson
    ) {
        dispatcher.dispatch(
                targetType, targetId, vendorName, communicationType, sender,
                null, payload, payloadJson, DEFAULT_ATTEMPT_COUNT
        );
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            V vendorName,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        dispatcher.dispatch(
                targetType, null, vendorName, communicationType, sender,
                exception, payload, payloadJson, DEFAULT_ATTEMPT_COUNT
        );
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            String targetId,
            V vendorName,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        dispatcher.dispatch(
                targetType, targetId, vendorName, communicationType, sender,
                exception, payload, payloadJson, DEFAULT_ATTEMPT_COUNT
        );
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            V vendorName,
            String payload,
            JsonNode payloadJson,
            int attemptCount
    ) {
        dispatcher.dispatch(
                targetType, null, vendorName, communicationType, sender,
                null, payload, payloadJson, attemptCount
        );
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            String targetId,
            V vendorName,
            String payload,
            JsonNode payloadJson,
            int attemptCount
    ) {
        dispatcher.dispatch(
                targetType, targetId, vendorName, communicationType, sender,
                null, payload, payloadJson, attemptCount
        );
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            V vendorName,
            String payload,
            JsonNode payloadJson,
            String exception,
            int attemptCount
    ) {
        dispatcher.dispatch(
                targetType, null, vendorName, communicationType, sender,
                exception, payload, payloadJson, attemptCount
        );
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            String targetId,
            V vendorName,
            String payload,
            JsonNode payloadJson,
            String exception,
            int attemptCount
    ) {
        dispatcher.dispatch(
                targetType, targetId, vendorName, communicationType, sender,
                exception, payload, payloadJson, attemptCount
        );
    }
}
