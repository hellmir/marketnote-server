package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentApprovedEvent;
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
@DisplayName("PaymentApprovedLedgerConsumer 테스트")
class PaymentApprovedLedgerConsumerTest {
    @InjectMocks
    private PaymentApprovedLedgerConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, String orderKey, Long paymentAmount) {
        PaymentApprovedEvent event = new PaymentApprovedEvent(orderId, orderKey, paymentAmount);
        EventEnvelope<PaymentApprovedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.approved", "commerce-service",
                LocalDateTime.of(2026, 3, 5, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.payment.approved", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("듀얼 라이트 기간 중 결제 승인 이벤트 수신 시 페이로드 검증을 완료하고 acknowledge한다")
    void handlePaymentApprovedEvent_success_validatesAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", 50000L);

        // when
        consumer.handlePaymentApprovedEvent(record, acknowledgment);

        // then
        // TODO: [#929][#933] RecordLedgerEntryUseCase 활성화 시
        //  recordLedgerEntryUseCase.recordPaymentApproval() 호출 검증 추가
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handlePaymentApprovedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "order-key-1", 50000L);

        // when
        consumer.handlePaymentApprovedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 이벤트를 무시하고 acknowledge한다")
    void handlePaymentApprovedEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, "order-key-1", 50000L);

        // when
        consumer.handlePaymentApprovedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 이벤트를 무시하고 acknowledge한다")
    void handlePaymentApprovedEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, "order-key-1", 50000L);

        // when
        consumer.handlePaymentApprovedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 이벤트를 무시하고 acknowledge한다")
    void handlePaymentApprovedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.approved", 0, 0L, "1", null
        );

        // when
        consumer.handlePaymentApprovedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(objectMapper);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 이벤트를 무시하고 acknowledge한다")
    void handlePaymentApprovedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        PaymentApprovedEvent event = new PaymentApprovedEvent(1L, "order-key-1", 50000L);
        EventEnvelope<PaymentApprovedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 3, 5, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.approved", 0, 0L, "1", envelope
        );

        // when
        consumer.handlePaymentApprovedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(objectMapper);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("페이로드 역직렬화 실패 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handlePaymentApprovedEvent_deserializationFailure_propagatesForRetry() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.approved", "commerce-service",
                LocalDateTime.of(2026, 3, 5, 10, 0), "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.approved", 0, 0L, "1", envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handlePaymentApprovedEvent(record, acknowledgment))
                .isInstanceOf(IllegalArgumentException.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
