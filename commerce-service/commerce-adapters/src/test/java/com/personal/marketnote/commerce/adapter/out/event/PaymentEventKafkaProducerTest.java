package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.event.exception.PaymentEventSerializationException;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentApprovedEvent;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventKafkaProducer 테스트")
class PaymentEventKafkaProducerTest {
    @InjectMocks
    private PaymentEventKafkaProducer paymentEventKafkaProducer;

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
    @DisplayName("결제 승인 이벤트 발행 시 올바른 토픽과 파티션 키로 Outbox에 저장된다")
    void publishPaymentApprovedEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-03-05T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        paymentEventKafkaProducer.publishPaymentApprovedEvent(1L, "order-key-1", 50000L);

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.PAYMENT_APPROVED);
        assertThat(captured.getPartitionKey()).isEqualTo("1");
        assertThat(captured.getSource()).isEqualTo("commerce-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("결제 승인 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishPaymentApprovedEvent_envelopeContainsCorrectPayload() throws Exception {
        // given
        setUpClock("2026-03-05T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        paymentEventKafkaProducer.publishPaymentApprovedEvent(10L, "order-key-10", 75000L);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.PAYMENT_APPROVED);
        assertThat(capturedEnvelope.source()).isEqualTo("commerce-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        PaymentApprovedEvent payload = (PaymentApprovedEvent) capturedEnvelope.payload();
        assertThat(payload.orderId()).isEqualTo(10L);
        assertThat(payload.orderKey()).isEqualTo("order-key-10");
        assertThat(payload.paymentAmount()).isEqualTo(75000L);
    }

    @Test
    @DisplayName("Outbox 저장 실패 시 예외를 전파한다")
    void publishPaymentApprovedEvent_propagatesExceptionWhenSaveFails() throws Exception {
        // given
        setUpClock("2026-11-21T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doThrow(new RuntimeException("DB connection failed"))
            .when(saveOutboxEventPort).save(any(OutboxEvent.class));

        // when & then
        assertThatThrownBy(() -> paymentEventKafkaProducer.publishPaymentApprovedEvent(1L, "order-key-1", 50000L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("DB connection failed");
    }

    @Test
    @DisplayName("Payload 직렬화 실패 시 PaymentEventSerializationException을 던진다")
    void publishPaymentApprovedEvent_throwsPaymentEventSerializationExceptionOnJsonProcessingFailure() throws Exception {
        // given
        setUpClock("2026-11-21T10:00:00Z");
        when(objectMapper.writeValueAsString(any()))
            .thenThrow(new TestJsonProcessingException("Serialization failed"));

        // when & then
        assertThatThrownBy(() -> paymentEventKafkaProducer.publishPaymentApprovedEvent(1L, "order-key-1", 50000L))
            .isInstanceOf(PaymentEventSerializationException.class)
            .hasCauseInstanceOf(JsonProcessingException.class);
        verifyNoInteractions(saveOutboxEventPort);
    }

    private static class TestJsonProcessingException extends JsonProcessingException {
        protected TestJsonProcessingException(String msg) {
            super(msg);
        }
    }
}
