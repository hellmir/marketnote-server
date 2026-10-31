package com.personal.marketnote.common.utility.audit;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 벤더 통신 이력 기록 요청을 서비스별 Command로 변환하여 위임하는 함수형 인터페이스.
 *
 * <p>서비스별 어댑터는 이 인터페이스를 구현하여 자신의 {@code XxxVendorCommunicationHistoryCommand}를
 * 조립하고 {@code RecordXxxVendorCommunicationHistoryUseCase}에 전달한다.</p>
 *
 * @param <T>  TargetType enum 타입
 * @param <V>  VendorName enum 타입
 * @param <C>  CommunicationType enum 타입
 * @param <S>  SenderType enum 타입
 */
@FunctionalInterface
public interface VendorCommunicationHistoryDispatcher<
        T extends Enum<T>,
        V extends Enum<V>,
        C extends Enum<C>,
        S extends Enum<S>
> {
    void dispatch(
            T targetType,
            String targetId,
            V vendorName,
            C communicationType,
            S sender,
            String exception,
            String payload,
            JsonNode payloadJson
    );
}
