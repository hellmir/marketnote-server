package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.user.SaveShippingAddressReadModelPort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangeAction;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingAddressChangedReadModelConsumer 테스트")
class ShippingAddressChangedReadModelConsumerTest {

    @InjectMocks
    private ShippingAddressChangedReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveShippingAddressReadModelPort saveShippingAddressReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.SHIPPING_ADDRESS_CHANGED, 0, 0, "100", envelope
        );
    }

    private EventEnvelope<ShippingAddressChangedEvent> createEnvelope(ShippingAddressChangedEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.SHIPPING_ADDRESS_CHANGED,
                "user-service",
                LocalDateTime.of(2026, 3, 31, 10, 0),
                payload
        );
    }

    @Test
    @DisplayName("CREATED 이벤트 수신 시 Read Model을 upsert한다")
    void handleShippingAddressChangedEvent_created_upsertsReadModel() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL", ShippingAddressChangeAction.CREATED
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verify(saveShippingAddressReadModelPort).upsert(1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("UPDATED 이벤트 수신 시 Read Model을 upsert한다")
    void handleShippingAddressChangedEvent_updated_upsertsReadModel() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                2L, 200L, "김철수", "010-9876-5432", "서울시 서초구 서초대로 456", "202동 303호", "JEJU", ShippingAddressChangeAction.UPDATED
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verify(saveShippingAddressReadModelPort).upsert(2L, 200L, "김철수", "010-9876-5432", "서울시 서초구 서초대로 456", "202동 303호", "JEJU");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("DELETED 이벤트 수신 시 Read Model을 비활성화한다")
    void handleShippingAddressChangedEvent_deleted_deactivatesReadModel() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", null, ShippingAddressChangeAction.DELETED
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verify(saveShippingAddressReadModelPort).deactivateByShippingAddressId(1L);
        verifyNoMoreInteractions(saveShippingAddressReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 즉시 acknowledge한다")
    void handleShippingAddressChangedEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.SHIPPING_ADDRESS_CHANGED, 0, 0, "100", null
        );

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingAddressReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge한다")
    void handleShippingAddressChangedEvent_eventTypeMismatch_acknowledges() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL", ShippingAddressChangeAction.CREATED
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "user-service",
                LocalDateTime.of(2026, 3, 31, 10, 0),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingAddressReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("shippingAddressId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleShippingAddressChangedEvent_nullShippingAddressId_acknowledges() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                null, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL", ShippingAddressChangeAction.CREATED
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingAddressReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleShippingAddressChangedEvent_nullUserId_acknowledges() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                1L, null, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL", ShippingAddressChangeAction.CREATED
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingAddressReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("action이 null이면 이벤트를 무시하고 acknowledge한다")
    void handleShippingAddressChangedEvent_nullAction_acknowledges() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL", null
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingAddressReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("shippingAddressId가 0이면 이벤트를 무시하고 acknowledge한다")
    void handleShippingAddressChangedEvent_zeroShippingAddressId_acknowledges() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                0L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL", ShippingAddressChangeAction.CREATED
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingAddressReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("shippingAddressId가 음수이면 이벤트를 무시하고 acknowledge한다")
    void handleShippingAddressChangedEvent_negativeShippingAddressId_acknowledges() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                -1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL", ShippingAddressChangeAction.CREATED
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingAddressReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("CREATED 이벤트에서 recipientName이 null이면 이벤트를 무시하고 acknowledge한다")
    void handleShippingAddressChangedEvent_nullRecipientName_acknowledges() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                1L, 100L, null, "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL", ShippingAddressChangeAction.CREATED
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingAddressReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("CREATED 이벤트에서 address가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleShippingAddressChangedEvent_nullAddress_acknowledges() {
        // given
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(
                1L, 100L, "홍길동", "010-1234-5678", null, "101동 1001호", "NORMAL", ShippingAddressChangeAction.CREATED
        );
        EventEnvelope<ShippingAddressChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingAddressChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingAddressReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("페이로드 역직렬화 실패 시 예외가 전파된다")
    void handleShippingAddressChangedEvent_deserializationFailure_propagatesException() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.SHIPPING_ADDRESS_CHANGED,
                "user-service",
                LocalDateTime.of(2026, 3, 31, 10, 0),
                "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.SHIPPING_ADDRESS_CHANGED, 0, 0, "100", envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handleShippingAddressChangedEvent(record, acknowledgment))
                .isInstanceOf(IllegalArgumentException.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
