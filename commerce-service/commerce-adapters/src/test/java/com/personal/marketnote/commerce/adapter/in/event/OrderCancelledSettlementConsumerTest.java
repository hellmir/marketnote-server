package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.commerce.domain.ledger.IdempotencyKey;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationSnapshotState;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
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
import org.springframework.dao.DataIntegrityViolationException;
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
        return buildRecord(orderId, null);
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, String cancelId) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                orderId, "order-key-1", 1L, 50000L, 50000L, 0L, 3000L,
                true, 0L, List.of(), List.of(), cancelId
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
                .idempotencyKey(IdempotencyKey.of("ORDER_ALLOCATION:" + orderId + ":" + sellerId))
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
        assertThat(cancellation.getAllocatedAmount()).isEqualTo(Money.of(50000L));
        assertThat(cancellation.getShippingFee()).isEqualTo(Money.of(3000L));
        assertThat(cancellation.getTransactionType()).isEqualTo(PaymentAllocationTransactionType.CANCELLATION);
        assertThat(cancellation.getIdempotencyKey().getValue()).isEqualTo("ORDER_CANCELLATION_ALLOCATION:100:10");

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

    @Test
    @DisplayName("cancelId 포함 이벤트 수신 시 idempotencyKey에 cancelId가 포함된다")
    @SuppressWarnings("unchecked")
    void handleOrderCancelledEvent_withCancelId_includesCancelIdInIdempotencyKey() {
        // given
        Long orderId = 100L;
        String cancelId = "cancel-uuid-123";
        PaymentAllocation original = createOriginalAllocation(1L, orderId, 10L, 50000L, 3000L);
        when(findPaymentAllocationPort.findByOrderId(orderId)).thenReturn(List.of(original));

        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(orderId, cancelId);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        ArgumentCaptor<List<PaymentAllocation>> captor = ArgumentCaptor.forClass(List.class);
        verify(savePaymentAllocationPort).saveAll(captor.capture());

        PaymentAllocation cancellation = captor.getValue().get(0);
        assertThat(cancellation.getIdempotencyKey().getValue())
                .isEqualTo("ORDER_CANCELLATION_ALLOCATION:100:10:cancel-uuid-123");
    }

    @Test
    @DisplayName("cancelId가 null인 이벤트 수신 시 기존 형식 idempotencyKey를 사용한다")
    @SuppressWarnings("unchecked")
    void handleOrderCancelledEvent_nullCancelId_usesLegacyIdempotencyKey() {
        // given
        Long orderId = 200L;
        PaymentAllocation original = createOriginalAllocation(2L, orderId, 20L, 40000L, 3000L);
        when(findPaymentAllocationPort.findByOrderId(orderId)).thenReturn(List.of(original));

        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(orderId, null);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        ArgumentCaptor<List<PaymentAllocation>> captor = ArgumentCaptor.forClass(List.class);
        verify(savePaymentAllocationPort).saveAll(captor.capture());

        PaymentAllocation cancellation = captor.getValue().get(0);
        assertThat(cancellation.getIdempotencyKey().getValue())
                .isEqualTo("ORDER_CANCELLATION_ALLOCATION:200:20");
    }

    @Test
    @DisplayName("동일 orderId/sellerId에 다른 cancelId로 부분취소가 2회 발생하면 두 번째 역배분도 정상 생성된다")
    @SuppressWarnings("unchecked")
    void handleOrderCancelledEvent_distinctCancelIds_bothAllocationsPersisted() {
        // given
        Long orderId = 300L;
        PaymentAllocation original = createOriginalAllocation(3L, orderId, 30L, 30000L, 3000L);
        when(findPaymentAllocationPort.findByOrderId(orderId)).thenReturn(List.of(original));

        ConsumerRecord<String, EventEnvelope<?>> firstRecord = buildRecord(orderId, "cancel-1");
        ConsumerRecord<String, EventEnvelope<?>> secondRecord = buildRecord(orderId, "cancel-2");

        // when
        consumer.handleOrderCancelledEvent(firstRecord, acknowledgment);
        consumer.handleOrderCancelledEvent(secondRecord, acknowledgment);

        // then
        ArgumentCaptor<List<PaymentAllocation>> captor = ArgumentCaptor.forClass(List.class);
        verify(savePaymentAllocationPort, times(2)).saveAll(captor.capture());

        List<List<PaymentAllocation>> allValues = captor.getAllValues();
        assertThat(allValues.get(0).get(0).getIdempotencyKey().getValue())
                .isEqualTo("ORDER_CANCELLATION_ALLOCATION:300:30:cancel-1");
        assertThat(allValues.get(1).get(0).getIdempotencyKey().getValue())
                .isEqualTo("ORDER_CANCELLATION_ALLOCATION:300:30:cancel-2");
    }

    @Test
    @DisplayName("동일 cancelId로 재발행되어 유니크 제약 위반 시 멱등 처리하고 acknowledge한다")
    void handleOrderCancelledEvent_duplicateCancelId_idempotentlyAcknowledges() {
        // given
        Long orderId = 400L;
        PaymentAllocation original = createOriginalAllocation(4L, orderId, 40L, 20000L, 3000L);
        when(findPaymentAllocationPort.findByOrderId(orderId)).thenReturn(List.of(original));
        doThrow(new DataIntegrityViolationException("duplicate idempotency key"))
                .when(savePaymentAllocationPort).saveAll(anyList());

        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(orderId, "cancel-duplicate");

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }
}
