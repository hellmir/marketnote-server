package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.InventoryChangeAction;
import com.personal.marketnote.common.kafka.event.InventoryChangedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEventKafkaProducer 테스트")
class InventoryEventKafkaProducerTest {
    @InjectMocks
    private InventoryEventKafkaProducer inventoryEventKafkaProducer;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    private void setUpClock(String instant) {
        Clock fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("재고 변경 이벤트 발행 시 올바른 토픽과 파티션 키로 Outbox에 저장된다")
    void publishInventoryChangedEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-03-31T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        inventoryEventKafkaProducer.publishInventoryChangedEvent(
                100L, 1L, 50, InventoryChangeAction.UPDATED
        );

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.INVENTORY_CHANGED);
        assertThat(captured.getPartitionKey()).isEqualTo("100");
        assertThat(captured.getSource()).isEqualTo("commerce-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("재고 변경 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishInventoryChangedEvent_envelopeContainsCorrectPayload() throws Exception {
        // given
        setUpClock("2026-03-31T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        inventoryEventKafkaProducer.publishInventoryChangedEvent(
                200L, 10L, 30, InventoryChangeAction.CREATED
        );

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.INVENTORY_CHANGED);
        assertThat(capturedEnvelope.source()).isEqualTo("commerce-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        InventoryChangedEvent payload = (InventoryChangedEvent) capturedEnvelope.payload();
        assertThat(payload.pricePolicyId()).isEqualTo(200L);
        assertThat(payload.productId()).isEqualTo(10L);
        assertThat(payload.stockQuantity()).isEqualTo(30);
        assertThat(payload.action()).isEqualTo(InventoryChangeAction.CREATED);
    }
}
