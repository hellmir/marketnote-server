package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateInventoryRestorationException;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent.OrderProductItem;
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
@DisplayName("PaymentCancelledInventoryConsumer 테스트")
class PaymentCancelledInventoryConsumerTest {
    @InjectMocks
    private PaymentCancelledInventoryConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RestoreProductInventoryUseCase restoreProductInventoryUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    private List<OrderProductItem> createOrderProductItems() {
        return List.of(
                new OrderProductItem(100L, 200L, 2, 30000L),
                new OrderProductItem(101L, null, 1, 20000L)
        );
    }

    private List<OrderProductItem> createCancelProductItems() {
        return List.of(
                new OrderProductItem(100L, 200L, 1, 30000L)
        );
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, boolean isFullCancel,
                                                                  List<OrderProductItem> orderProducts,
                                                                  List<OrderProductItem> cancelProducts) {
        PaymentCancelledEvent event = new PaymentCancelledEvent(
                orderId, "order-key-1", 100L, 50000L, 80000L, 1000L,
                isFullCancel, 0L, null, orderProducts, cancelProducts, null
        );
        EventEnvelope<PaymentCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.cancelled", "commerce-service",
                LocalDateTime.of(2026, 3, 6, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.payment.cancelled", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("전체 취소 이벤트 수신 시 재고를 복구하고 acknowledge한다")
    void handlePaymentCancelledEvent_fullCancel_restoresInventoryAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, orderProducts, null);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(restoreProductInventoryUseCase).restore(argThat(products ->
                products.size() == 2
                        && products.get(0).getPricePolicyId().equals(100L)
                        && products.get(0).getQuantity() == 2
                        && products.get(1).getPricePolicyId().equals(101L)
                        && products.get(1).getQuantity() == 1
        ), eq(1L), eq("Kafka 전액 취소 재고 복구"));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 취소 이벤트 수신 시 cancelProducts로 재고를 복구하고 acknowledge한다")
    void handlePaymentCancelledEvent_partialCancel_restoresInventoryAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = createOrderProductItems();
        List<OrderProductItem> cancelProducts = createCancelProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, orderProducts, cancelProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(restoreProductInventoryUseCase).restore(argThat(products ->
                products.size() == 1
                        && products.get(0).getPricePolicyId().equals(100L)
                        && products.get(0).getQuantity() == 1
        ), eq(1L), eq("Kafka 부분 취소 재고 복구"));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 재고 복구 시 DuplicateInventoryRestorationException을 catch하고 acknowledge한다")
    void handlePaymentCancelledEvent_duplicateRestoration_catchesAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, orderProducts, null);
        doThrow(new DuplicateInventoryRestorationException(1L))
                .when(restoreProductInventoryUseCase).restore(anyList(), anyLong(), anyString());

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 재고 복구 없이 acknowledge한다")
    void handlePaymentCancelledEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, true, orderProducts, null);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(restoreProductInventoryUseCase, never()).restore(anyList(), anyLong(), anyString());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("전체 취소인데 orderProducts가 비어있으면 재고 복구 없이 acknowledge한다")
    void handlePaymentCancelledEvent_fullCancelEmptyOrderProducts_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, List.of(), null);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(restoreProductInventoryUseCase, never()).restore(anyList(), anyLong(), anyString());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("전체 취소인데 orderProducts가 null이면 재고 복구 없이 acknowledge한다")
    void handlePaymentCancelledEvent_fullCancelNullOrderProducts_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, null, null);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(restoreProductInventoryUseCase, never()).restore(anyList(), anyLong(), anyString());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 취소인데 cancelProducts가 비어있으면 재고 복구 없이 acknowledge한다")
    void handlePaymentCancelledEvent_partialCancelEmptyCancelProducts_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, orderProducts, List.of());

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(restoreProductInventoryUseCase, never()).restore(anyList(), anyLong(), anyString());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 취소인데 cancelProducts가 null이면 재고 복구 없이 acknowledge한다")
    void handlePaymentCancelledEvent_partialCancelNullCancelProducts_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, orderProducts, null);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(restoreProductInventoryUseCase, never()).restore(anyList(), anyLong(), anyString());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    @SuppressWarnings("unchecked")
    void handlePaymentCancelledEvent_unexpectedException_propagatesForRetry() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.cancelled", "commerce-service",
                LocalDateTime.of(2026, 3, 6, 10, 0), "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1",
                (EventEnvelope<?>) (EventEnvelope) envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handlePaymentCancelledEvent(record, acknowledgment))
                .isInstanceOf(Exception.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
