package com.personal.marketnote.commerce.adapter.out.event;

import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentApprovedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventKafkaProducer 테스트")
class PaymentEventKafkaProducerTest {
    @InjectMocks
    private PaymentEventKafkaProducer paymentEventKafkaProducer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private Clock clock;

    private void setUpClock(String instant) {
        Clock fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("결제 승인 이벤트 발행 시 올바른 토픽과 파티션 키로 전송된다")
    void publishPaymentApprovedEvent_sendsToCorrectTopicWithOrderIdKey() {
        // given
        setUpClock("2026-03-05T10:00:00Z");
        when(kafkaTemplate.send(any(String.class), any(String.class), any()))
                .thenReturn(new CompletableFuture<>());

        // when
        paymentEventKafkaProducer.publishPaymentApprovedEvent(1L, "order-key-1", 50000L);

        // then
        verify(kafkaTemplate).send(
                eq(KafkaTopicConstants.PAYMENT_APPROVED),
                eq("1"),
                any(EventEnvelope.class)
        );
    }

    @Test
    @DisplayName("결제 승인 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishPaymentApprovedEvent_envelopeContainsCorrectPayload() {
        // given
        setUpClock("2026-03-05T10:00:00Z");
        when(kafkaTemplate.send(any(String.class), any(String.class), any()))
                .thenReturn(new CompletableFuture<>());

        // when
        paymentEventKafkaProducer.publishPaymentApprovedEvent(10L, "order-key-10", 75000L);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(kafkaTemplate).send(
                eq(KafkaTopicConstants.PAYMENT_APPROVED),
                eq("10"),
                envelopeCaptor.capture()
        );

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
}
