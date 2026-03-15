package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCancelledLedgerConsumer 테스트")
class PaymentCancelledLedgerConsumerTest {
    @InjectMocks
    private PaymentCancelledLedgerConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, boolean isFullCancel) {
        PaymentCancelledEvent event = new PaymentCancelledEvent(
                orderId, "order-key-1", 1L, 50000L, 100000L, 0L,
                isFullCancel, 0L, "cancel-id-1",
                List.of(new PaymentCancelledEvent.OrderProductItem(1L, null, 2, 50000L)),
                null, null
        );
        EventEnvelope<PaymentCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.cancelled", "commerce-service",
                LocalDateTime.of(2026, 3, 9, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.payment.cancelled", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("듀얼 라이트 기간 중 전체 취소 이벤트 수신 시 페이로드 검증을 완료하고 acknowledge한다")
    void handlePaymentCancelledEvent_fullCancel_validatesAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        // TODO: [#929][#1023] RecordLedgerEntryUseCase 활성화 시
        //  recordLedgerEntryUseCase.recordPaymentCancellation() 호출 검증 추가
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("듀얼 라이트 기간 중 부분 취소 이벤트 수신 시 페이로드 검증을 완료하고 acknowledge한다")
    void handlePaymentCancelledEvent_partialCancel_validatesAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handlePaymentCancelledEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, true);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 이벤트를 무시하고 acknowledge한다")
    void handlePaymentCancelledEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, true);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 이벤트를 무시하고 acknowledge한다")
    void handlePaymentCancelledEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, true);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 이벤트를 무시하고 acknowledge한다")
    void handlePaymentCancelledEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1", null
        );

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(objectMapper);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 이벤트를 무시하고 acknowledge한다")
    void handlePaymentCancelledEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        PaymentCancelledEvent event = new PaymentCancelledEvent(
                1L, "order-key-1", 1L, 50000L, 100000L, 0L,
                true, 0L, "cancel-id-1",
                List.of(new PaymentCancelledEvent.OrderProductItem(1L, null, 2, 50000L)),
                null, null
        );
        EventEnvelope<PaymentCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 3, 9, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1", envelope
        );

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(objectMapper);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("페이로드 역직렬화 실패 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handlePaymentCancelledEvent_deserializationFailure_propagatesForRetry() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.cancelled", "commerce-service",
                LocalDateTime.of(2026, 3, 9, 10, 0), "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1", envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handlePaymentCancelledEvent(record, acknowledgment))
                .isInstanceOf(IllegalArgumentException.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
