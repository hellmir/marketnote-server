package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.settlement.*;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelledSettlementConsumer 테스트")
class OrderCancelledSettlementConsumerTest {
    @InjectMocks
    private OrderCancelledSettlementConsumer consumer;

    @Mock
    private FindPaymentAllocationPort findPaymentAllocationPort;

    @Mock
    private SavePaymentAllocationPort savePaymentAllocationPort;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                orderId, "order-key-1", 1L, 50000L, 50000L, 0L, 3000L,
                true, 0L, List.of(), List.of()
        );
        EventEnvelope<OrderCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.cancelled", "commerce-service",
                LocalDateTime.of(2026, 4, 8, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.cancelled", 0, 0L, String.valueOf(orderId), envelope);
    }

    private PaymentAllocation createOriginalAllocation(Long id, Long orderId, Long sellerId,
                                                       Long allocatedAmount, Long shippingFee) {
        return PaymentAllocation.from(PaymentAllocationSnapshotState.builder()
                .id(id)
                .orderId(orderId)
                .sellerId(sellerId)
                .allocatedAmount(allocatedAmount)
                .shippingFee(shippingFee)
                .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                .targetType(PaymentAllocationTargetType.ORDER)
                .idempotencyKey("ORDER_ALLOCATION:" + orderId + ":" + sellerId)
                .createdAt(LocalDateTime.of(2026, 4, 7, 10, 0))
                .build());
    }

    @Test
    @DisplayName("주문 취소 시 원래 배분에 대응하는 CANCELLATION PaymentAllocation을 생성한다")
    @SuppressWarnings("unchecked")
    void handleOrderCancelledEvent_createsCancellationAllocations() {
        // given
        Long orderId = 100L;
        PaymentAllocation original = createOriginalAllocation(1L, orderId, 10L, 50000L, 3000L);
        when(findPaymentAllocationPort.findByOrderId(orderId)).thenReturn(List.of(original));

        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(orderId);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        ArgumentCaptor<List<PaymentAllocation>> captor = ArgumentCaptor.forClass(List.class);
        verify(savePaymentAllocationPort).saveAll(captor.capture());
        List<PaymentAllocation> cancellations = captor.getValue();

        assertThat(cancellations).hasSize(1);
        PaymentAllocation cancellation = cancellations.get(0);
        assertThat(cancellation.getOrderId()).isEqualTo(orderId);
        assertThat(cancellation.getSellerId()).isEqualTo(10L);
        assertThat(cancellation.getAllocatedAmount()).isEqualTo(50000L);
        assertThat(cancellation.getShippingFee()).isEqualTo(3000L);
        assertThat(cancellation.getTransactionType()).isEqualTo(PaymentAllocationTransactionType.CANCELLATION);
        assertThat(cancellation.getIdempotencyKey()).isEqualTo("ORDER_CANCELLATION_ALLOCATION:100:10");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("원래 배분이 없으면 CANCELLATION을 생성하지 않는다")
    void handleOrderCancelledEvent_noOriginalAllocations_skips() {
        // given
        Long orderId = 100L;
        when(findPaymentAllocationPort.findByOrderId(orderId)).thenReturn(List.of());

        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(orderId);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verify(savePaymentAllocationPort, never()).saveAll(anyList());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleOrderCancelledEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(findPaymentAllocationPort, savePaymentAllocationPort);
        verify(acknowledgment).acknowledge();
    }
}
