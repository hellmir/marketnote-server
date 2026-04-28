package com.personal.marketnote.user.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangeAction;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangedEvent;
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
@DisplayName("ShippingAddressEventKafkaProducer 테스트")
class ShippingAddressEventKafkaProducerTest {

    @InjectMocks
    private ShippingAddressEventKafkaProducer shippingAddressEventKafkaProducer;

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
    @DisplayName("배송지 변경 이벤트 발행 시 올바른 토픽과 파티션 키로 Outbox에 저장된다")
    void publishShippingAddressChangedEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-03-31T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        shippingAddressEventKafkaProducer.publishShippingAddressChangedEvent(
                1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL", ShippingAddressChangeAction.CREATED
        );

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.SHIPPING_ADDRESS_CHANGED);
        assertThat(captured.getPartitionKey()).isEqualTo("100");
        assertThat(captured.getSource()).isEqualTo("user-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("배송지 등록 이벤트 발행 시 EventEnvelope에 올바른 CREATED 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishShippingAddressChangedEvent_envelopeContainsCorrectCreatedPayload() throws Exception {
        // given
        setUpClock("2026-03-31T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        shippingAddressEventKafkaProducer.publishShippingAddressChangedEvent(
                1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL", ShippingAddressChangeAction.CREATED
        );

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.SHIPPING_ADDRESS_CHANGED);
        assertThat(capturedEnvelope.source()).isEqualTo("user-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        ShippingAddressChangedEvent payload = (ShippingAddressChangedEvent) capturedEnvelope.payload();
        assertThat(payload.shippingAddressId()).isEqualTo(1L);
        assertThat(payload.userId()).isEqualTo(100L);
        assertThat(payload.recipientName()).isEqualTo("홍길동");
        assertThat(payload.recipientPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(payload.address()).isEqualTo("서울시 강남구 테헤란로 123");
        assertThat(payload.addressDetail()).isEqualTo("101동 1001호");
        assertThat(payload.action()).isEqualTo(ShippingAddressChangeAction.CREATED);
    }

    @Test
    @DisplayName("배송지 수정 이벤트 발행 시 UPDATED 액션으로 Outbox에 저장된다")
    @SuppressWarnings("unchecked")
    void publishShippingAddressChangedEvent_updatedAction_envelopeContainsUpdatedPayload() throws Exception {
        // given
        setUpClock("2026-03-31T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        shippingAddressEventKafkaProducer.publishShippingAddressChangedEvent(
                2L, 200L, "김철수", "010-9876-5432", "서울시 서초구 서초대로 456", "202동 303호", "NORMAL", ShippingAddressChangeAction.UPDATED
        );

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        ShippingAddressChangedEvent payload = (ShippingAddressChangedEvent) envelopeCaptor.getValue().payload();
        assertThat(payload.shippingAddressId()).isEqualTo(2L);
        assertThat(payload.userId()).isEqualTo(200L);
        assertThat(payload.action()).isEqualTo(ShippingAddressChangeAction.UPDATED);
    }

    @Test
    @DisplayName("배송지 삭제 이벤트 발행 시 DELETED 액션으로 Outbox에 저장된다")
    @SuppressWarnings("unchecked")
    void publishShippingAddressChangedEvent_deletedAction_envelopeContainsDeletedPayload() throws Exception {
        // given
        setUpClock("2026-03-31T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        shippingAddressEventKafkaProducer.publishShippingAddressChangedEvent(
                3L, 300L, "이영희", "010-5555-6666", "서울시 마포구 월드컵로 789", "301동 501호", null, ShippingAddressChangeAction.DELETED
        );

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        ShippingAddressChangedEvent payload = (ShippingAddressChangedEvent) envelopeCaptor.getValue().payload();
        assertThat(payload.shippingAddressId()).isEqualTo(3L);
        assertThat(payload.userId()).isEqualTo(300L);
        assertThat(payload.action()).isEqualTo(ShippingAddressChangeAction.DELETED);
    }
}
