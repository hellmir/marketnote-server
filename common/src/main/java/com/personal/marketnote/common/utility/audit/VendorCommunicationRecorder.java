package com.personal.marketnote.common.utility.audit;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 벤더 통신 이력을 기록하는 공통 유틸리티.
 *
 * <p>서비스별 Recorder 중복을 제거하기 위해 target/vendor/type/sender enum과 dispatcher를 타입 파라미터로 받아
 * 4종 오버로드(record)를 제공한다. 서비스별 어댑터는 {@link VendorCommunicationHistoryDispatcher}를
 * 주입하여 자신의 UseCase로 위임한다.</p>
 *
 * @param <T>  TargetType enum 타입
 * @param <V>  VendorName enum 타입
 * @param <C>  CommunicationType enum 타입
 * @param <S>  SenderType enum 타입
 */
public class VendorCommunicationRecorder<
        T extends Enum<T>,
        V extends Enum<V>,
        C extends Enum<C>,
        S extends Enum<S>
> {
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
        dispatcher.dispatch(targetType, null, vendorName, communicationType, sender, null, payload, payloadJson);
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
        dispatcher.dispatch(targetType, targetId, vendorName, communicationType, sender, null, payload, payloadJson);
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
        dispatcher.dispatch(targetType, null, vendorName, communicationType, sender, exception, payload, payloadJson);
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
                targetType, targetId, vendorName, communicationType, sender, exception, payload, payloadJson
        );
    }
}
