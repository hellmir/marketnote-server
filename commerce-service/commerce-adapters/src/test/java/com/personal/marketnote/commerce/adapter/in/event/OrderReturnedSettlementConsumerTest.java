package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationSnapshotState;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
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
@DisplayName("OrderReturnedSettlementConsumer 테스트")
class OrderReturnedSettlementConsumerTest {
    @InjectMocks
    private OrderReturnedSettlementConsumer consumer;

    @Mock
    private FindPaymentAllocationPort findPaymentAllocationPort;

    @Mock
    private SavePaymentAllocationPort savePaymentAllocationPort;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId) {
        OrderReturnedEvent event = new OrderReturnedEvent(
                orderId, "order-key-1", 1L, 50000L, 50000L, 0L, 3000L,
                true, 2500L, List.of()
        );
        EventEnvelope<OrderReturnedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.returned", "commerce-service",
                LocalDateTime.of(2026, 4, 9, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.returned", 0, 0L, String.valueOf(orderId), envelope);
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
    @DisplayName("반품 완료 시 원래 배분에 대응하는 RETURN_REFUND PaymentAllocation을 생성한다")
    @SuppressWarnings("unchecked")
    void handleOrderReturnedEvent_createsReturnAllocations() {
        // given
        Long orderId = 100L;
        PaymentAllocation original = createOriginalAllocation(1L, orderId, 10L, 50000L, 3000L);
        when(findPaymentAllocationPort.findByOrderId(orderId)).thenReturn(List.of(original));

        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(orderId);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<List<PaymentAllocation>> captor = ArgumentCaptor.forClass(List.class);
        verify(savePaymentAllocationPort).saveAll(captor.capture());
        List<PaymentAllocation> returnAllocations = captor.getValue();

        assertThat(returnAllocations).hasSize(1);
        PaymentAllocation returnAllocation = returnAllocations.get(0);
        assertThat(returnAllocation.getOrderId()).isEqualTo(orderId);
        assertThat(returnAllocation.getSellerId()).isEqualTo(10L);
        assertThat(returnAllocation.getAllocatedAmount()).isEqualTo(50000L);
        assertThat(returnAllocation.getShippingFee()).isEqualTo(3000L);
        assertThat(returnAllocation.getTransactionType()).isEqualTo(PaymentAllocationTransactionType.RETURN_REFUND);
        assertThat(returnAllocation.getIdempotencyKey()).isEqualTo("ORDER_RETURN_ALLOCATION:100:10");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("복수 판매자 배분이 있으면 각각에 대해 RETURN_REFUND를 생성한다")
    @SuppressWarnings("unchecked")
    void handleOrderReturnedEvent_multipleSellers_createsReturnAllocationsForEach() {
        // given
        Long orderId = 200L;
        PaymentAllocation seller1 = createOriginalAllocation(1L, orderId, 10L, 30000L, 2000L);
        PaymentAllocation seller2 = createOriginalAllocation(2L, orderId, 20L, 20000L, 1000L);
        when(findPaymentAllocationPort.findByOrderId(orderId)).thenReturn(List.of(seller1, seller2));

        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(orderId);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<List<PaymentAllocation>> captor = ArgumentCaptor.forClass(List.class);
        verify(savePaymentAllocationPort).saveAll(captor.capture());
        List<PaymentAllocation> returnAllocations = captor.getValue();

        assertThat(returnAllocations).hasSize(2);
        assertThat(returnAllocations.get(0).getSellerId()).isEqualTo(10L);
        assertThat(returnAllocations.get(0).getIdempotencyKey()).isEqualTo("ORDER_RETURN_ALLOCATION:200:10");
        assertThat(returnAllocations.get(1).getSellerId()).isEqualTo(20L);
        assertThat(returnAllocations.get(1).getIdempotencyKey()).isEqualTo("ORDER_RETURN_ALLOCATION:200:20");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("원래 배분이 없으면 RETURN_REFUND를 생성하지 않는다")
    void handleOrderReturnedEvent_noOriginalAllocations_skips() {
        // given
        Long orderId = 100L;
        when(findPaymentAllocationPort.findByOrderId(orderId)).thenReturn(List.of());

        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(orderId);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verify(savePaymentAllocationPort, never()).saveAll(anyList());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleOrderReturnedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(findPaymentAllocationPort, savePaymentAllocationPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 이벤트를 무시하고 acknowledge한다")
    void handleOrderReturnedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.returned", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(findPaymentAllocationPort, savePaymentAllocationPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 이벤트를 무시하고 acknowledge한다")
    void handleOrderReturnedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        OrderReturnedEvent event = new OrderReturnedEvent(
                1L, "order-key-1", 1L, 50000L, 50000L, 0L, 3000L,
                true, 2500L, List.of()
        );
        EventEnvelope<OrderReturnedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 4, 9, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.returned", 0, 0L, "1", envelope
        );

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(findPaymentAllocationPort, savePaymentAllocationPort);
        verify(acknowledgment).acknowledge();
    }
}
