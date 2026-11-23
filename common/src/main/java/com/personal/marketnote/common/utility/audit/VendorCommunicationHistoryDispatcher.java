package com.personal.marketnote.common.utility.audit;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 벤더 통신 이력 기록 요청을 서비스별 Command로 변환하여 위임하는 함수형 인터페이스.
 *
 * <p>서비스별 어댑터는 이 인터페이스를 구현하여 자신의 {@code XxxVendorCommunicationHistoryCommand}를
 * 조립하고 {@code RecordXxxVendorCommunicationHistoryUseCase}에 전달한다.</p>
 *
 * @param <T> TargetType enum 타입
 * @param <V> VendorName enum 타입
 * @param <C> CommunicationType enum 타입
 * @param <S> SenderType enum 타입
 */
@FunctionalInterface
public interface VendorCommunicationHistoryDispatcher<
        T extends Enum<T>,
        V extends Enum<V>,
        C extends Enum<C>,
        S extends Enum<S>
        > {
    /**
     * 벤더 통신 이력 1건을 위임 처리한다.
     *
     * @param targetType        통신 대상 유형
     * @param targetId          통신 대상 식별자(없으면 {@code null})
     * @param vendorName        벤더 이름
     * @param communicationType REQUEST/RESPONSE 등 통신 유형
     * @param sender            발신 주체
     * @param exception         예외 메시지(없으면 {@code null})
     * @param payload           원문 페이로드
     * @param payloadJson       JSON 페이로드(없으면 {@code null})
     * @param attemptCount      이번 통신의 시도 횟수(1=최초 호출, 2 이상=재시도). 단일 호출 경로의 기본값은 1.
     */
    void dispatch(
            T targetType,
            String targetId,
            V vendorName,
            C communicationType,
            S sender,
            String exception,
            String payload,
            JsonNode payloadJson,
            int attemptCount
    );
}
