package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.fulfillment.SaveFulfillmentWorkStatusReadModelPort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.FulfillmentDeliveryWorkStatusChangedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentDeliveryWorkStatusChangedReadModelConsumer 테스트")
class FulfillmentDeliveryWorkStatusChangedReadModelConsumerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-09-17T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    @InjectMocks
    private FulfillmentDeliveryWorkStatusChangedReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveFulfillmentWorkStatusReadModelPort saveFulfillmentWorkStatusReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private EventEnvelope<FulfillmentDeliveryWorkStatusChangedEvent> createEnvelope(
            FulfillmentDeliveryWorkStatusChangedEvent payload) {
        return EventEnvelope.of(
                KafkaTopicConstants.FULFILLMENT_DELIVERY_WORK_STATUS_CHANGED,
                "fulfillment-service",
                payload,
                FIXED_CLOCK
        );
    }

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.FULFILLMENT_DELIVERY_WORK_STATUS_CHANGED, 0, 0, "1", envelope
        );
    }

    @Test
    @DisplayName("envelope가 null이면 ack만 호출하고 포트는 호출하지 않는다")
    void nullEnvelope_acknowledgesWithoutUpsert() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.FULFILLMENT_DELIVERY_WORK_STATUS_CHANGED, 0, 0, "1", null
        );

        // when
        consumer.handleFulfillmentDeliveryWorkStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveFulfillmentWorkStatusReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 일치하지 않으면 ack만 호출하고 포트는 호출하지 않는다")
    void eventTypeMismatch_acknowledgesWithoutUpsert() {
        // given
        FulfillmentDeliveryWorkStatusChangedEvent payload =
                new FulfillmentDeliveryWorkStatusChangedEvent(100L, "PICKING");
        EventEnvelope<FulfillmentDeliveryWorkStatusChangedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "fulfillment-service",
                java.time.LocalDateTime.now(FIXED_CLOCK),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleFulfillmentDeliveryWorkStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveFulfillmentWorkStatusReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 ack만 호출하고 포트는 호출하지 않는다")
    void nullOrderId_acknowledgesWithoutUpsert() {
        // given
        FulfillmentDeliveryWorkStatusChangedEvent payload =
                new FulfillmentDeliveryWorkStatusChangedEvent(null, "PICKING");
        EventEnvelope<FulfillmentDeliveryWorkStatusChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleFulfillmentDeliveryWorkStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveFulfillmentWorkStatusReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("workStatus가 null이면 ack만 호출하고 포트는 호출하지 않는다")
    void nullWorkStatus_acknowledgesWithoutUpsert() {
        // given
        FulfillmentDeliveryWorkStatusChangedEvent payload =
                new FulfillmentDeliveryWorkStatusChangedEvent(100L, null);
        EventEnvelope<FulfillmentDeliveryWorkStatusChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleFulfillmentDeliveryWorkStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveFulfillmentWorkStatusReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("유효한 이벤트이면 upsert 포트를 호출하고 ack한다")
    void validEvent_upsertsAndAcknowledges() {
        // given
        FulfillmentDeliveryWorkStatusChangedEvent payload =
                new FulfillmentDeliveryWorkStatusChangedEvent(100L, "PICKING");
        EventEnvelope<FulfillmentDeliveryWorkStatusChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleFulfillmentDeliveryWorkStatusChangedEvent(record, acknowledgment);

        // then
        verify(saveFulfillmentWorkStatusReadModelPort).upsert(100L, "PICKING");
        verify(acknowledgment).acknowledge();
    }
}
