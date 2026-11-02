package com.personal.marketnote.product.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ShippingPolicyChangeAction;
import com.personal.marketnote.common.kafka.event.ShippingPolicyChangedEvent;
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
@DisplayName("ShippingPolicyEventKafkaProducer 테스트")
class ShippingPolicyEventKafkaProducerTest {

    @InjectMocks
    private ShippingPolicyEventKafkaProducer shippingPolicyEventKafkaProducer;

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
    @DisplayName("배송비 정책 변경 이벤트 발행 시 올바른 토픽과 파티션 키로 Outbox에 저장된다")
    void publishShippingPolicyChangedEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-03-31T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        shippingPolicyEventKafkaProducer.publishShippingPolicyChangedEvent(
                10L, 3000L, 20000L, 3000L, 5000L, ShippingPolicyChangeAction.CREATED
        );

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.SHIPPING_POLICY_CHANGED);
        assertThat(captured.getPartitionKey()).isEqualTo("10");
        assertThat(captured.getSource()).isEqualTo("product-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("배송비 정책 변경 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishShippingPolicyChangedEvent_envelopeContainsCorrectPayload() throws Exception {
        // given
        setUpClock("2026-03-31T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        shippingPolicyEventKafkaProducer.publishShippingPolicyChangedEvent(
                10L, 3000L, 20000L, 3000L, 5000L, ShippingPolicyChangeAction.CREATED
        );

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.SHIPPING_POLICY_CHANGED);
        assertThat(capturedEnvelope.source()).isEqualTo("product-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        ShippingPolicyChangedEvent payload = (ShippingPolicyChangedEvent) capturedEnvelope.payload();
        assertThat(payload.sellerId()).isEqualTo(10L);
        assertThat(payload.shippingFee()).isEqualTo(3000L);
        assertThat(payload.freeShippingThreshold()).isEqualTo(20000L);
        assertThat(payload.jejuSurcharge()).isEqualTo(3000L);
        assertThat(payload.islandSurcharge()).isEqualTo(5000L);
        assertThat(payload.action()).isEqualTo(ShippingPolicyChangeAction.CREATED);
    }

    @Test
    @DisplayName("배송비 정책 수정 이벤트 발행 시 UPDATED 액션으로 Outbox에 저장된다")
    @SuppressWarnings("unchecked")
    void publishShippingPolicyChangedEvent_updatedAction_envelopeContainsUpdatedPayload() throws Exception {
        // given
        setUpClock("2026-03-31T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        shippingPolicyEventKafkaProducer.publishShippingPolicyChangedEvent(
                20L, 2500L, 30000L, 4000L, 6000L, ShippingPolicyChangeAction.UPDATED
        );

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        ShippingPolicyChangedEvent payload = (ShippingPolicyChangedEvent) envelopeCaptor.getValue().payload();
        assertThat(payload.sellerId()).isEqualTo(20L);
        assertThat(payload.shippingFee()).isEqualTo(2500L);
        assertThat(payload.freeShippingThreshold()).isEqualTo(30000L);
        assertThat(payload.jejuSurcharge()).isEqualTo(4000L);
        assertThat(payload.islandSurcharge()).isEqualTo(6000L);
        assertThat(payload.action()).isEqualTo(ShippingPolicyChangeAction.UPDATED);
    }
}
