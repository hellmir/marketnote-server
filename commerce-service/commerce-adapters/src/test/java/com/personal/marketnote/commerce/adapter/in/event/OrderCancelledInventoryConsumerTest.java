package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.exception.DuplicateInventoryRestorationException;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent.OrderProductItem;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelledInventoryConsumer 테스트")
class OrderCancelledInventoryConsumerTest {
    @InjectMocks
    private OrderCancelledInventoryConsumer consumer;

    @Mock
    private RestoreProductInventoryUseCase restoreProductInventoryUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private static final UUID SHARER_KEY = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, boolean isFullCancel,
                                                                 List<OrderProductItem> orderProducts,
                                                                 List<OrderProductItem> cancelProducts) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                orderId, "order-key-1", 50L, 60000L, 80000L, 1000L, 3000L,
                isFullCancel, 0L, orderProducts, cancelProducts
        );
        EventEnvelope<OrderCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.cancelled", "commerce-service",
                LocalDateTime.of(2026, 4, 8, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.cancelled", 0, 0L, String.valueOf(orderId), envelope);
    }

    private List<OrderProductItem> createOrderProductItems() {
        return List.of(
                new OrderProductItem(100L, SHARER_KEY, 2, 30000L),
                new OrderProductItem(101L, null, 1, 20000L)
        );
    }

    private List<OrderProductItem> createCancelProductItems() {
        return List.of(new OrderProductItem(100L, SHARER_KEY, 2, 30000L));
    }

    @Test
    @DisplayName("전체 취소 이벤트 수신 시 orderProducts로 재고를 복구하고 acknowledge한다")
    @SuppressWarnings("unchecked")
    void handleOrderCancelledEvent_fullCancel_restoresInventoryWithOrderProductsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, orderProducts, List.of());

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        ArgumentCaptor<List<OrderProduct>> productsCaptor = ArgumentCaptor.forClass(List.class);
        verify(restoreProductInventoryUseCase).restore(productsCaptor.capture(), eq(1L), eq("주문 취소 전액 재고 복구"));

        List<OrderProduct> capturedProducts = productsCaptor.getValue();
        assertThat(capturedProducts).hasSize(2);
        assertThat(capturedProducts.get(0).getPricePolicyId()).isEqualTo(100L);
        assertThat(capturedProducts.get(0).getQuantity()).isEqualTo(2);
        assertThat(capturedProducts.get(1).getPricePolicyId()).isEqualTo(101L);
        assertThat(capturedProducts.get(1).getQuantity()).isEqualTo(1);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 취소 이벤트 수신 시 cancelProducts로 재고를 복구하고 acknowledge한다")
    @SuppressWarnings("unchecked")
    void handleOrderCancelledEvent_partialCancel_restoresInventoryWithCancelProductsAndAcknowledges() {
        // given
        List<OrderProductItem> cancelProducts = createCancelProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(2L, false, createOrderProductItems(), cancelProducts);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        ArgumentCaptor<List<OrderProduct>> productsCaptor = ArgumentCaptor.forClass(List.class);
        verify(restoreProductInventoryUseCase).restore(productsCaptor.capture(), eq(2L), eq("주문 취소 부분 재고 복구"));

        List<OrderProduct> capturedProducts = productsCaptor.getValue();
        assertThat(capturedProducts).hasSize(1);
        assertThat(capturedProducts.get(0).getPricePolicyId()).isEqualTo(100L);
        assertThat(capturedProducts.get(0).getSharerKey()).isEqualTo(SHARER_KEY);
        assertThat(capturedProducts.get(0).getQuantity()).isEqualTo(2);
        assertThat(capturedProducts.get(0).getUnitAmount()).isEqualTo(30000L);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("전체 취소인데 orderProducts가 비어있으면 재고 복구를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_fullCancelEmptyProducts_skipsRestoreAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, List.of(), List.of());

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 취소인데 cancelProducts가 비어있으면 재고 복구를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_partialCancelEmptyProducts_skipsRestoreAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(2L, false, createOrderProductItems(), List.of());

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 취소인데 cancelProducts가 null이면 재고 복구를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_partialCancelNullProducts_skipsRestoreAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(2L, false, createOrderProductItems(), null);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 처리된 재고 복구 이벤트에 대해 DuplicateInventoryRestorationException 발생 시 멱등 처리로 정상 acknowledge한다")
    void handleOrderCancelledEvent_duplicateRestoration_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, createOrderProductItems(), List.of());
        doThrow(new DuplicateInventoryRestorationException(1L))
                .when(restoreProductInventoryUseCase).restore(anyList(), eq(1L), anyString());

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verify(restoreProductInventoryUseCase).restore(anyList(), eq(1L), eq("주문 취소 전액 재고 복구"));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.cancelled", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        OrderCancelledEvent event = new OrderCancelledEvent(
                1L, "order-key-1", 50L, 60000L, 80000L, 1000L, 3000L,
                true, 0L, createOrderProductItems(), List.of()
        );
        EventEnvelope<OrderCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 4, 8, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.cancelled", 0, 0L, "1", envelope
        );

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, true, createOrderProductItems(), List.of());

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, true, createOrderProductItems(), List.of());

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, true, createOrderProductItems(), List.of());

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleOrderCancelledEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, createOrderProductItems(), List.of());
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(restoreProductInventoryUseCase).restore(anyList(), eq(1L), anyString());

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderCancelledEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 오류");

        verify(restoreProductInventoryUseCase).restore(anyList(), eq(1L), eq("주문 취소 전액 재고 복구"));
        verify(acknowledgment, never()).acknowledge();
    }
}
