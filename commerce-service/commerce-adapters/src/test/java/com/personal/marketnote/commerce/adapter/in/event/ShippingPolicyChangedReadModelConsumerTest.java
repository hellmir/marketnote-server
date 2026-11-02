package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.shipping.SaveShippingPolicyReadModelPort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ShippingPolicyChangeAction;
import com.personal.marketnote.common.kafka.event.ShippingPolicyChangedEvent;
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
@DisplayName("ShippingPolicyChangedReadModelConsumer 테스트")
class ShippingPolicyChangedReadModelConsumerTest {

    @InjectMocks
    private ShippingPolicyChangedReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveShippingPolicyReadModelPort saveShippingPolicyReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.SHIPPING_POLICY_CHANGED, 0, 0, "10", envelope
        );
    }

    private EventEnvelope<ShippingPolicyChangedEvent> createEnvelope(ShippingPolicyChangedEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.SHIPPING_POLICY_CHANGED,
                "product-service",
                LocalDateTime.of(2026, 3, 31, 10, 0),
                payload
        );
    }

    @Test
    @DisplayName("CREATED 이벤트 수신 시 Read Model을 upsert한다")
    void handleShippingPolicyChangedEvent_created_upsertsReadModel() {
        // given
        ShippingPolicyChangedEvent payload = new ShippingPolicyChangedEvent(
                10L, 3000L, 20000L, 3000L, 5000L, ShippingPolicyChangeAction.CREATED
        );
        EventEnvelope<ShippingPolicyChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingPolicyChangedEvent(record, acknowledgment);

        // then
        verify(saveShippingPolicyReadModelPort).upsert(10L, 3000L, 20000L, 3000L, 5000L);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("UPDATED 이벤트 수신 시 Read Model을 upsert한다")
    void handleShippingPolicyChangedEvent_updated_upsertsReadModel() {
        // given
        ShippingPolicyChangedEvent payload = new ShippingPolicyChangedEvent(
                20L, 2500L, 30000L, 4000L, 6000L, ShippingPolicyChangeAction.UPDATED
        );
        EventEnvelope<ShippingPolicyChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingPolicyChangedEvent(record, acknowledgment);

        // then
        verify(saveShippingPolicyReadModelPort).upsert(20L, 2500L, 30000L, 4000L, 6000L);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("DELETED 이벤트 수신 시 Read Model을 비활성화한다")
    void handleShippingPolicyChangedEvent_deleted_deactivatesReadModel() {
        // given
        ShippingPolicyChangedEvent payload = new ShippingPolicyChangedEvent(
                10L, 3000L, 20000L, 3000L, 5000L, ShippingPolicyChangeAction.DELETED
        );
        EventEnvelope<ShippingPolicyChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingPolicyChangedEvent(record, acknowledgment);

        // then
        verify(saveShippingPolicyReadModelPort).deactivateBySellerId(10L);
        verifyNoMoreInteractions(saveShippingPolicyReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 즉시 acknowledge한다")
    void handleShippingPolicyChangedEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.SHIPPING_POLICY_CHANGED, 0, 0, "10", null
        );

        // when
        consumer.handleShippingPolicyChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingPolicyReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge한다")
    void handleShippingPolicyChangedEvent_eventTypeMismatch_acknowledges() {
        // given
        ShippingPolicyChangedEvent payload = new ShippingPolicyChangedEvent(
                10L, 3000L, 20000L, 3000L, 5000L, ShippingPolicyChangeAction.CREATED
        );
        EventEnvelope<ShippingPolicyChangedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "product-service",
                LocalDateTime.of(2026, 3, 31, 10, 0),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingPolicyChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingPolicyReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("sellerId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleShippingPolicyChangedEvent_nullSellerId_acknowledges() {
        // given
        ShippingPolicyChangedEvent payload = new ShippingPolicyChangedEvent(
                null, 3000L, 20000L, 3000L, 5000L, ShippingPolicyChangeAction.CREATED
        );
        EventEnvelope<ShippingPolicyChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingPolicyChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingPolicyReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("sellerId가 0이면 이벤트를 무시하고 acknowledge한다")
    void handleShippingPolicyChangedEvent_zeroSellerId_acknowledges() {
        // given
        ShippingPolicyChangedEvent payload = new ShippingPolicyChangedEvent(
                0L, 3000L, 20000L, 0L, 0L, ShippingPolicyChangeAction.CREATED
        );
        EventEnvelope<ShippingPolicyChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingPolicyChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingPolicyReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("sellerId가 음수이면 이벤트를 무시하고 acknowledge한다")
    void handleShippingPolicyChangedEvent_negativeSellerId_acknowledges() {
        // given
        ShippingPolicyChangedEvent payload = new ShippingPolicyChangedEvent(
                -1L, 3000L, 20000L, 0L, 0L, ShippingPolicyChangeAction.CREATED
        );
        EventEnvelope<ShippingPolicyChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleShippingPolicyChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveShippingPolicyReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("페이로드 역직렬화 실패 시 예외가 전파된다")
    void handleShippingPolicyChangedEvent_deserializationFailure_propagatesException() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.SHIPPING_POLICY_CHANGED,
                "product-service",
                LocalDateTime.of(2026, 3, 31, 10, 0),
                "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.SHIPPING_POLICY_CHANGED, 0, 0, "10", envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handleShippingPolicyChangedEvent(record, acknowledgment))
                .isInstanceOf(IllegalArgumentException.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
