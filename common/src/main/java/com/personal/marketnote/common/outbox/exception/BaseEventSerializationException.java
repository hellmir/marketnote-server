package com.personal.marketnote.common.outbox.exception;

/**
 * Outbox 이벤트 직렬화 실패의 공통 베이스 예외.
 * 운영 트러블슈팅을 위해 topic/partitionKey/eventId 컨텍스트를 메시지에 포함한다.
 */
public abstract class BaseEventSerializationException extends RuntimeException {

    protected BaseEventSerializationException(
        String errorCode, String domainLabel,
        String topic, String partitionKey, String eventId,
        Throwable cause
    ) {
        super(
            errorCode + "::" + domainLabel + " 직렬화에 실패했습니다."
                + " topic=" + topic
                + ", partitionKey=" + partitionKey
                + ", eventId=" + eventId,
            cause
        );
    }
}
