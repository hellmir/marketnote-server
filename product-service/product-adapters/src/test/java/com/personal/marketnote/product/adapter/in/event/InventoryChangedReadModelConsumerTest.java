package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.InventoryChangeAction;
import com.personal.marketnote.common.kafka.event.InventoryChangedEvent;
import com.personal.marketnote.product.port.out.inventory.SaveInventoryReadModelPort;
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
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryChangedReadModelConsumerTest {

    @InjectMocks
    private InventoryChangedReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveInventoryReadModelPort saveInventoryReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.INVENTORY_CHANGED, 0, 0, "1", envelope
        );
    }

    private EventEnvelope<InventoryChangedEvent> createEnvelope(InventoryChangedEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.INVENTORY_CHANGED,
                "commerce-service",
                LocalDateTime.now(Clock.fixed(Instant.parse("2026-03-31T10:00:00Z"), ZoneId.of("Asia/Seoul"))),
                payload
        );
    }

    @Test
    @DisplayName("envelope가 null이면 즉시 acknowledge하고 upsert를 호출하지 않는다")
    void handleInventoryChangedEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.INVENTORY_CHANGED, 0, 0, "1", null
        );

        // when
        consumer.handleInventoryChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveInventoryReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge하고 upsert를 호출하지 않는다")
    void handleInventoryChangedEvent_eventTypeMismatch_acknowledges() {
        // given
        InventoryChangedEvent payload = new InventoryChangedEvent(
                1L, 100L, 50, InventoryChangeAction.CREATED
        );
        EventEnvelope<InventoryChangedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "commerce-service",
                LocalDateTime.now(),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleInventoryChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveInventoryReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pricePolicyId가 null이면 즉시 acknowledge하고 upsert를 호출하지 않는다")
    void handleInventoryChangedEvent_nullPricePolicyId_acknowledges() {
        // given
        InventoryChangedEvent payload = new InventoryChangedEvent(
                null, 100L, 50, InventoryChangeAction.CREATED
        );
        EventEnvelope<InventoryChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleInventoryChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveInventoryReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pricePolicyId가 0이면 즉시 acknowledge하고 upsert를 호출하지 않는다")
    void handleInventoryChangedEvent_zeroPricePolicyId_acknowledges() {
        // given
        InventoryChangedEvent payload = new InventoryChangedEvent(
                0L, 100L, 50, InventoryChangeAction.CREATED
        );
        EventEnvelope<InventoryChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleInventoryChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveInventoryReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("action이 null이면 warn 로그 후 acknowledge하고 upsert를 호출하지 않는다")
    void handleInventoryChangedEvent_nullAction_acknowledges() {
        // given
        InventoryChangedEvent payload = new InventoryChangedEvent(
                1L, 100L, 50, null
        );
        EventEnvelope<InventoryChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleInventoryChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveInventoryReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("CREATED action 이벤트 수신 시 upsert를 호출한다")
    void handleInventoryChangedEvent_createdAction_upsertsReadModel() {
        // given
        InventoryChangedEvent payload = new InventoryChangedEvent(
                1L, 100L, 50, InventoryChangeAction.CREATED
        );
        EventEnvelope<InventoryChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleInventoryChangedEvent(record, acknowledgment);

        // then
        verify(saveInventoryReadModelPort).upsert(1L, 100L, 50);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("UPDATED action 이벤트 수신 시 upsert를 호출한다")
    void handleInventoryChangedEvent_updatedAction_upsertsReadModel() {
        // given
        InventoryChangedEvent payload = new InventoryChangedEvent(
                2L, 200L, 30, InventoryChangeAction.UPDATED
        );
        EventEnvelope<InventoryChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleInventoryChangedEvent(record, acknowledgment);

        // then
        verify(saveInventoryReadModelPort).upsert(2L, 200L, 30);
        verify(acknowledgment).acknowledge();
    }
}
