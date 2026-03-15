package com.personal.marketnote.common.kafka.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventPayloadValidator 테스트")
class EventPayloadValidatorTest {
    private static final String TOPIC = "commerce.payment.approved";
    private static final String EVENT_ID = "test-event-id";

    private EventEnvelope<?> buildEnvelope(String eventType) {
        return new EventEnvelope<>(EVENT_ID, eventType, "test-source",
                LocalDateTime.of(2026, 3, 9, 10, 0), "test-payload");
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(TOPIC, 0, 0L, "key", envelope);
    }

    @Nested
    @DisplayName("hasInvalidEnvelope")
    class HasInvalidEnvelope {

        @Test
        @DisplayName("envelope이 null이면 true를 반환한다")
        void nullEnvelope_returnsTrue() {
            // given
            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    TOPIC, 0, 0L, "key", null
            );

            // when
            boolean result = EventPayloadValidator.hasInvalidEnvelope(null, record);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("envelope이 유효하면 false를 반환한다")
        void validEnvelope_returnsFalse() {
            // given
            EventEnvelope<?> envelope = buildEnvelope(TOPIC);
            ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(envelope);

            // when
            boolean result = EventPayloadValidator.hasInvalidEnvelope(envelope, record);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("hasEventTypeMismatch")
    class HasEventTypeMismatch {

        @Test
        @DisplayName("eventType이 일치하면 false를 반환한다")
        void matchingEventType_returnsFalse() {
            // given
            EventEnvelope<?> envelope = buildEnvelope(TOPIC);

            // when
            boolean result = EventPayloadValidator.hasEventTypeMismatch(envelope, TOPIC);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("eventType이 불일치하면 true를 반환한다")
        void mismatchingEventType_returnsTrue() {
            // given
            EventEnvelope<?> envelope = buildEnvelope("commerce.payment.failed");

            // when
            boolean result = EventPayloadValidator.hasEventTypeMismatch(envelope, TOPIC);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("eventType이 null이면 true를 반환한다")
        void nullEventType_returnsTrue() {
            // given
            EventEnvelope<?> envelope = buildEnvelope(null);

            // when
            boolean result = EventPayloadValidator.hasEventTypeMismatch(envelope, TOPIC);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("eventType이 빈 문자열이면 true를 반환한다")
        void emptyEventType_returnsTrue() {
            // given
            EventEnvelope<?> envelope = buildEnvelope("");

            // when
            boolean result = EventPayloadValidator.hasEventTypeMismatch(envelope, TOPIC);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("hasInvalidIds")
    class HasInvalidIds {

        @Test
        @DisplayName("모든 ID가 양수이면 false를 반환한다")
        void allPositiveIds_returnsFalse() {
            // when
            boolean result = EventPayloadValidator.hasInvalidIds(EVENT_ID,
                    EventPayloadValidator.id("orderId", 1L),
                    EventPayloadValidator.id("buyerId", 100L));

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ID가 null이면 true를 반환한다")
        void nullId_returnsTrue() {
            // when
            boolean result = EventPayloadValidator.hasInvalidIds(EVENT_ID,
                    EventPayloadValidator.id("orderId", null));

            // then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("ID가 0 이하이면 true를 반환한다")
        void nonPositiveId_returnsTrue(Long value) {
            // when
            boolean result = EventPayloadValidator.hasInvalidIds(EVENT_ID,
                    EventPayloadValidator.id("orderId", value));

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("복수 ID 중 하나라도 유효하지 않으면 true를 반환한다")
        void oneInvalidAmongMultiple_returnsTrue() {
            // when
            boolean result = EventPayloadValidator.hasInvalidIds(EVENT_ID,
                    EventPayloadValidator.id("orderId", 1L),
                    EventPayloadValidator.id("buyerId", null));

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("필드가 없으면 false를 반환한다")
        void noFields_returnsFalse() {
            // when
            boolean result = EventPayloadValidator.hasInvalidIds(EVENT_ID);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("id 팩토리 메서드")
    class IdFactoryMethod {

        @Test
        @DisplayName("name과 value가 올바르게 설정된다")
        void createsIdFieldWithCorrectValues() {
            // when
            EventPayloadValidator.IdField field = EventPayloadValidator.id("orderId", 42L);

            // then
            assertThat(field.name()).isEqualTo("orderId");
            assertThat(field.value()).isEqualTo(42L);
        }
    }
}
