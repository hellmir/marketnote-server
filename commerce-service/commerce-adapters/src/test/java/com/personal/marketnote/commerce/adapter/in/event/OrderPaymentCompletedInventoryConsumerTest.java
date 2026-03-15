package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.exception.DuplicateInventoryDeductionException;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedInventoryConsumer 테스트")
class OrderPaymentCompletedInventoryConsumerTest {
    @InjectMocks
    private OrderPaymentCompletedInventoryConsumer consumer;

    @Mock
    private ReduceProductInventoryUseCase reduceProductInventoryUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long buyerId,
                                                                 Long totalAmount, Long pointAmount,
                                                                 List<OrderProductItem> orderProducts) {
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                orderId, buyerId, totalAmount, pointAmount, orderProducts, null
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.payment-completed", "commerce-service",
                LocalDateTime.of(2026, 3, 5, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.payment-completed", 0, 0L,
                String.valueOf(orderId), envelope);
    }

    private List<OrderProductItem> createOrderProductItems() {
        return List.of(
                new OrderProductItem(100L, 200L, 2, 30000L),
                new OrderProductItem(101L, null, 1, 20000L)
        );
    }

    @Test
    @DisplayName("주문 결제 완료 이벤트 수신 시 재고를 차감하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_success_reducesInventoryAndAcknowledges() {
        // given
        List<OrderProductItem> items = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 50L, 80000L, 5000L, items);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(reduceProductInventoryUseCase).reduce(
                anyList(), eq(1L), eq("Kafka 결제 완료 재고 차감")
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("재고 차감 시 주문 상품 목록이 올바르게 변환된다")
    void handleOrderPaymentCompletedEvent_success_convertsOrderProductsCorrectly() {
        // given
        List<OrderProductItem> items = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 50L, 80000L, 5000L, items);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<OrderProduct>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        verify(reduceProductInventoryUseCase).reduce(captor.capture(), eq(1L), anyString());

        List<OrderProduct> orderProducts = captor.getValue();
        assertThat(orderProducts).hasSize(2);
        assertThat(orderProducts.get(0).getPricePolicyId()).isEqualTo(100L);
        assertThat(orderProducts.get(0).getQuantity()).isEqualTo(2);
        assertThat(orderProducts.get(1).getPricePolicyId()).isEqualTo(101L);
        assertThat(orderProducts.get(1).getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("orderId가 null이면 재고 차감 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> items = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 50L, 80000L, 5000L, items);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(reduceProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("주문 상품이 없으면 재고 차감 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_emptyOrderProducts_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 50L, 80000L, 5000L, List.of());
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(reduceProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("주문 상품이 null이면 재고 차감 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullOrderProducts_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 50L, 80000L, 5000L, null);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(reduceProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 재고 차감 시 DuplicateInventoryDeductionException을 catch하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_duplicateDeduction_acknowledgesAndLogs() {
        // given
        List<OrderProductItem> items = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 50L, 80000L, 5000L, items);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        doThrow(new DuplicateInventoryDeductionException(1L))
                .when(reduceProductInventoryUseCase).reduce(anyList(), eq(1L), anyString());

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.payment-completed", 0, 0L, "1", null
        );
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(reduceProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> items = createOrderProductItems();
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                1L, 50L, 80000L, 5000L, items, null
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 3, 5, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.payment-completed", 0, 0L, "1", envelope
        );
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(reduceProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> items = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, 50L, 80000L, 5000L, items);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(reduceProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> items = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, 50L, 80000L, 5000L, items);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(reduceProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleOrderPaymentCompletedEvent_unexpectedException_propagatesForRetry() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.payment-completed", "commerce-service",
                LocalDateTime.of(2026, 3, 5, 10, 0), "invalid-payload"
        );
        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.payment-completed", 0, 0L, "1",
                (EventEnvelope<?>) (EventEnvelope) envelope
        );
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderPaymentCompletedEvent(record, acknowledgment))
                .isInstanceOf(Exception.class);

        verify(acknowledgment, never()).acknowledge();
    }

}
