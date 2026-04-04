package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingSharedPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingSharedPointUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedSharedPointConsumer 테스트")
class OrderPaymentCompletedSharedPointConsumerTest {
    private static final UUID SHARER_KEY_1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID SHARER_KEY_2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final UUID SHARER_KEY_3 = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");

    @InjectMocks
    private OrderPaymentCompletedSharedPointConsumer consumer;

    @Mock
    private ModifyPendingSharedPointUseCase modifyPendingSharedPointUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(consumer, "sharePointRate", 0.1f);
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long orderId, Long buyerId, Long totalAmount, List<OrderProductItem> orderProducts
    ) {
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                orderId, buyerId, totalAmount, 0L, orderProducts, null
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.payment-completed", "commerce-service",
                LocalDateTime.of(2026, 3, 8, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.payment-completed", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("공유자 2명이 있는 주문 결제 완료 이벤트 수신 시 각 공유자에게 적립 예정 포인트를 적립하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_withSharers_accumulatesAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L),
                new OrderProductItem(2L, SHARER_KEY_2, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ModifyPendingSharedPointCommand> captor = ArgumentCaptor.forClass(ModifyPendingSharedPointCommand.class);
        verify(modifyPendingSharedPointUseCase, times(2)).modifyPending(captor.capture());

        List<ModifyPendingSharedPointCommand> commands = captor.getAllValues();
        long expectedAmount = Math.round(50000L * 0.1f);

        // 첫 번째 공유자 (SHARER_KEY_1)
        ModifyPendingSharedPointCommand firstCommand = commands.get(0);
        assertThat(firstCommand.buyerId()).isEqualTo(100L);
        assertThat(firstCommand.sharerKey()).isEqualTo(SHARER_KEY_1);
        assertThat(firstCommand.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(firstCommand.amount()).isEqualTo(expectedAmount);
        assertThat(firstCommand.sourceType()).isEqualTo(UserPointSourceType.ORDER);
        assertThat(firstCommand.sourceId()).isEqualTo(1L);
        assertThat(firstCommand.reason()).isEqualTo("링크 공유 회원 상품 구매");

        // 두 번째 공유자 (SHARER_KEY_2)
        ModifyPendingSharedPointCommand secondCommand = commands.get(1);
        assertThat(secondCommand.buyerId()).isEqualTo(100L);
        assertThat(secondCommand.sharerKey()).isEqualTo(SHARER_KEY_2);
        assertThat(secondCommand.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(secondCommand.amount()).isEqualTo(expectedAmount);
        assertThat(secondCommand.sourceType()).isEqualTo(UserPointSourceType.ORDER);
        assertThat(secondCommand.sourceId()).isEqualTo(1L);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 공유자가 있으면 중복 제거 후 한 번만 적립하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_duplicateSharers_deduplicatesAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L),
                new OrderProductItem(2L, SHARER_KEY_1, 1, 20000L),
                new OrderProductItem(3L, SHARER_KEY_2, 1, 15000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(modifyPendingSharedPointUseCase, times(2)).modifyPending(any(ModifyPendingSharedPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("공유자가 없는 주문이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_noSharers_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, null, 2, 10000L),
                new OrderProductItem(2L, null, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 100L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullBuyerId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.payment-completed", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                1L, 100L, 50000L, 0L, orderProducts, null
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 3, 8, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.payment-completed", 0, 0L, "1", envelope
        );

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, 100L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, 100L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_zeroBuyerId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 0L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_negativeBuyerId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, -1L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 이벤트 수신 시 DuplicateUserPointHistoryException이 발생하면 멱등 처리하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_duplicateEvent_idempotentAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);
        doThrow(new DuplicateUserPointHistoryException(200L, UserPointSourceType.ORDER, 1L, "링크 공유 회원 상품 구매"))
                .when(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("공유자 2명 중 첫 번째만 중복이면 두 번째는 정상 적립하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_partialDuplicate_continuesAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L),
                new OrderProductItem(2L, SHARER_KEY_2, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);
        when(modifyPendingSharedPointUseCase.modifyPending(any(ModifyPendingSharedPointCommand.class)))
                .thenThrow(new DuplicateUserPointHistoryException(200L, UserPointSourceType.ORDER, 1L, "링크 공유 회원 상품 구매"))
                .thenReturn(null);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(modifyPendingSharedPointUseCase, times(2)).modifyPending(any(ModifyPendingSharedPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("공유자 전원 중복이면 모두 멱등 처리하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_allDuplicate_idempotentAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L),
                new OrderProductItem(2L, SHARER_KEY_2, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);
        when(modifyPendingSharedPointUseCase.modifyPending(any(ModifyPendingSharedPointCommand.class)))
                .thenThrow(new DuplicateUserPointHistoryException(200L, UserPointSourceType.ORDER, 1L, "링크 공유 회원 상품 구매"))
                .thenThrow(new DuplicateUserPointHistoryException(300L, UserPointSourceType.ORDER, 1L, "링크 공유 회원 상품 구매"));

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(modifyPendingSharedPointUseCase, times(2)).modifyPending(any(ModifyPendingSharedPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handleOrderPaymentCompletedEvent_unexpectedException_propagatesWithoutAcknowledge() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderPaymentCompletedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 실패");

        verify(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("orderProducts가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullOrderProducts_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, null);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("totalAmount가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullTotalAmount_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, null, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("totalAmount가 0이면 적립 금액이 0이므로 적립을 생략하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_zeroTotalAmount_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 0L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }
}
