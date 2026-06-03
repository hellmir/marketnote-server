package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.exception.DuplicateInventoryRestorationException;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent.OrderProductItem;
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
@DisplayName("OrderReturnedInventoryConsumer 테스트")
class OrderReturnedInventoryConsumerTest {
    @InjectMocks
    private OrderReturnedInventoryConsumer consumer;

    @Mock
    private RestoreProductInventoryUseCase restoreProductInventoryUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private static final UUID SHARER_KEY = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, boolean isFullReturn,
                                                                 List<OrderProductItem> returnProducts) {
        OrderReturnedEvent event = new OrderReturnedEvent(
                orderId, "order-key-1", 50L, 60000L, 80000L, 1000L, 3000L,
                isFullReturn, 2500L, returnProducts
        );
        EventEnvelope<OrderReturnedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.returned", "commerce-service",
                LocalDateTime.of(2026, 4, 9, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.returned", 0, 0L, String.valueOf(orderId), envelope);
    }

    private List<OrderProductItem> createReturnProductItems() {
        return List.of(
                new OrderProductItem(100L, SHARER_KEY, 2, 30000L),
                new OrderProductItem(101L, null, 1, 20000L)
        );
    }

    private List<OrderProductItem> createSingleReturnProductItem() {
        return List.of(new OrderProductItem(100L, SHARER_KEY, 2, 30000L));
    }

    @Test
    @DisplayName("전체 반품 이벤트 수신 시 returnProducts로 재고를 복구하고 acknowledge한다")
    @SuppressWarnings("unchecked")
    void handleOrderReturnedEvent_fullReturn_restoresInventoryAndAcknowledges() {
        // given
        List<OrderProductItem> returnProducts = createReturnProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, returnProducts);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<List<OrderProduct>> productsCaptor = ArgumentCaptor.forClass(List.class);
        verify(restoreProductInventoryUseCase).restore(productsCaptor.capture(), eq(1L), eq("반품 완료 전체 재고 복구"));

        List<OrderProduct> capturedProducts = productsCaptor.getValue();
        assertThat(capturedProducts).hasSize(2);
        assertThat(capturedProducts.get(0).getPricePolicyId()).isEqualTo(100L);
        assertThat(capturedProducts.get(0).getQuantity()).isEqualTo(2);
        assertThat(capturedProducts.get(0).getSharerKey()).isEqualTo(SHARER_KEY);
        assertThat(capturedProducts.get(1).getPricePolicyId()).isEqualTo(101L);
        assertThat(capturedProducts.get(1).getQuantity()).isEqualTo(1);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 반품 이벤트 수신 시 returnProducts로 재고를 복구하고 acknowledge한다")
    @SuppressWarnings("unchecked")
    void handleOrderReturnedEvent_partialReturn_restoresInventoryAndAcknowledges() {
        // given
        List<OrderProductItem> returnProducts = createSingleReturnProductItem();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(2L, false, returnProducts);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<List<OrderProduct>> productsCaptor = ArgumentCaptor.forClass(List.class);
        verify(restoreProductInventoryUseCase).restore(productsCaptor.capture(), eq(2L), eq("반품 완료 부분 재고 복구"));

        List<OrderProduct> capturedProducts = productsCaptor.getValue();
        assertThat(capturedProducts).hasSize(1);
        assertThat(capturedProducts.get(0).getPricePolicyId()).isEqualTo(100L);
        assertThat(capturedProducts.get(0).getSharerKey()).isEqualTo(SHARER_KEY);
        assertThat(capturedProducts.get(0).getQuantity()).isEqualTo(2);
        assertThat(capturedProducts.get(0).getUnitAmount()).isEqualTo(30000L);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("returnProducts가 비어있으면 재고 복구를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_emptyReturnProducts_skipsRestoreAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, List.of());

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("returnProducts가 null이면 재고 복구를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_nullReturnProducts_skipsRestoreAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, null);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 처리된 재고 복구 이벤트에 대해 DuplicateInventoryRestorationException 발생 시 멱등 처리로 정상 acknowledge한다")
    void handleOrderReturnedEvent_duplicateRestoration_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, createReturnProductItems());
        doThrow(new DuplicateInventoryRestorationException(1L))
                .when(restoreProductInventoryUseCase).restore(anyList(), eq(1L), anyString());

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verify(restoreProductInventoryUseCase).restore(anyList(), eq(1L), eq("반품 완료 전체 재고 복구"));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.returned", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        OrderReturnedEvent event = new OrderReturnedEvent(
                1L, "order-key-1", 50L, 60000L, 80000L, 1000L, 3000L,
                true, 2500L, createReturnProductItems()
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
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, true, createReturnProductItems());

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, true, createReturnProductItems());

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, true, createReturnProductItems());

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("returnProducts에 pricePolicyId가 0인 항목이 있으면 재고 복구를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_zeroPricePolicyId_skipsRestoreAndAcknowledges() {
        // given
        List<OrderProductItem> invalidItems = List.of(
                new OrderProductItem(0L, SHARER_KEY, 2, 30000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, invalidItems);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("returnProducts에 quantity가 0인 항목이 있으면 재고 복구를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_zeroQuantity_skipsRestoreAndAcknowledges() {
        // given
        List<OrderProductItem> invalidItems = List.of(
                new OrderProductItem(100L, SHARER_KEY, 0, 30000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, invalidItems);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("returnProducts에 음수 quantity 항목이 있으면 재고 복구를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_negativeQuantity_skipsRestoreAndAcknowledges() {
        // given
        List<OrderProductItem> invalidItems = List.of(
                new OrderProductItem(100L, SHARER_KEY, -1, 30000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, invalidItems);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(restoreProductInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleOrderReturnedEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, createReturnProductItems());
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(restoreProductInventoryUseCase).restore(anyList(), eq(1L), anyString());

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderReturnedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 오류");

        verify(restoreProductInventoryUseCase).restore(anyList(), eq(1L), eq("반품 완료 전체 재고 복구"));
        verify(acknowledgment, never()).acknowledge();
    }
}
