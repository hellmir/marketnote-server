package com.personal.marketnote.common.outbox.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BaseEventSerializationException 메시지 포맷 검증")
class BaseEventSerializationExceptionTest {

    @Test
    @DisplayName("메시지에 errorCode, domainLabel, topic, partitionKey, eventId가 모두 포함된다")
    void message_includesAllContextFields() {
        Throwable cause = new RuntimeException("json error");
        TestEventSerializationException exception = new TestEventSerializationException(
            "topic-a", "pk-1", "evt-1", cause
        );

        assertThat(exception.getMessage())
            .contains("ERR_TEST_EVENT_01")
            .contains("테스트 이벤트")
            .contains("topic=topic-a")
            .contains("partitionKey=pk-1")
            .contains("eventId=evt-1");
    }

    @Test
    @DisplayName("원인 예외(cause)를 보존한다")
    void cause_isPreserved() {
        Throwable cause = new RuntimeException("json error");
        TestEventSerializationException exception = new TestEventSerializationException(
            "topic-a", "pk-1", "evt-1", cause
        );

        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("partitionKey가 null이어도 메시지가 정상적으로 구성된다")
    void message_handlesNullPartitionKey() {
        TestEventSerializationException exception = new TestEventSerializationException(
            "topic-a", null, "evt-1", new RuntimeException()
        );

        assertThat(exception.getMessage()).contains("partitionKey=null");
    }

    @Test
    @DisplayName("eventId가 null이어도 메시지가 정상적으로 구성된다")
    void message_handlesNullEventId() {
        TestEventSerializationException exception = new TestEventSerializationException(
            "topic-a", "pk-1", null, new RuntimeException()
        );

        assertThat(exception.getMessage()).contains("eventId=null");
    }

    @Test
    @DisplayName("RuntimeException을 상속한다")
    void extendsRuntimeException() {
        TestEventSerializationException exception = new TestEventSerializationException(
            "topic-a", "pk-1", "evt-1", new RuntimeException()
        );

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(BaseEventSerializationException.class);
    }

    private static class TestEventSerializationException extends BaseEventSerializationException {
        TestEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
            super("ERR_TEST_EVENT_01", "테스트 이벤트", topic, partitionKey, eventId, cause);
        }
    }
}
