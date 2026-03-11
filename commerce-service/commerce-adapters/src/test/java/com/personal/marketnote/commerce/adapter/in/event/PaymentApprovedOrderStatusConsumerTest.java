package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentApprovedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static com.personal.marketnote.commerce.domain.order.OrderStatus.PAID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentApprovedOrderStatusConsumer 테스트")
class PaymentApprovedOrderStatusConsumerTest {
    @InjectMocks
    private PaymentApprovedOrderStatusConsumer consumer;

    @Mock
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;

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
    @DisplayName("결제 승인 이벤트 수신 시 주문 상태를 PAID로 변경하고 acknowledge한다")
    void handlePaymentApprovedEvent_success_changesOrderStatusToPaidAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", 50000L);

        // when
        consumer.handlePaymentApprovedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
        verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

        ChangeOrderStatusCommand command = captor.getValue();
        assertThat(command.id()).isEqualTo(1L);
        assertThat(command.orderStatus()).isEqualTo(PAID);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handlePaymentApprovedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "order-key-1", 50000L);

        // when
        consumer.handlePaymentApprovedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handlePaymentApprovedEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", 50000L);
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handlePaymentApprovedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 오류");

        verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
