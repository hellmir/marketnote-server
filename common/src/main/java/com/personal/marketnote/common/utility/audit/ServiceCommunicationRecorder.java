package com.personal.marketnote.common.utility.audit;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 서비스 간 통신 이력을 기록하는 공통 유틸리티.
 *
 * <p>서비스별 Recorder 중복을 제거하기 위해 target/type/sender enum과 dispatcher를 타입 파라미터로 받아
 * 4종 오버로드(record)를 제공한다. 서비스별 어댑터는 {@link ServiceCommunicationHistoryDispatcher}를
 * 주입하여 자신의 UseCase로 위임한다.</p>
 *
 * @param <T>  TargetType enum 타입
 * @param <C>  CommunicationType enum 타입
 * @param <S>  SenderType enum 타입
 */
public class ServiceCommunicationRecorder<
        T extends Enum<T>,
        C extends Enum<C>,
        S extends Enum<S>
> {
    private final ServiceCommunicationHistoryDispatcher<T, C, S> dispatcher;

    public ServiceCommunicationRecorder(ServiceCommunicationHistoryDispatcher<T, C, S> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void record(T targetType, C communicationType, S sender, String payload, JsonNode payloadJson) {
        dispatcher.dispatch(targetType, null, communicationType, sender, null, payload, payloadJson);
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            String targetId,
            String payload,
            JsonNode payloadJson
    ) {
        dispatcher.dispatch(targetType, targetId, communicationType, sender, null, payload, payloadJson);
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        dispatcher.dispatch(targetType, null, communicationType, sender, exception, payload, payloadJson);
    }

    public void record(
            T targetType,
            C communicationType,
            S sender,
            String targetId,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        dispatcher.dispatch(targetType, targetId, communicationType, sender, exception, payload, payloadJson);
    }
}
