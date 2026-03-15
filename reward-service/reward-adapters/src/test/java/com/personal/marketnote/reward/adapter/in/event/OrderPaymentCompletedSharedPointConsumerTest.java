package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedSharedPointConsumer 테스트")
class OrderPaymentCompletedSharedPointConsumerTest {
    @InjectMocks
    private OrderPaymentCompletedSharedPointConsumer consumer;

    @Mock
    private ModifyPendingPointUseCase modifyPendingPointUseCase;

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
                new OrderProductItem(1L, 200L, 2, 10000L),
                new OrderProductItem(2L, 300L, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ModifyPendingPointCommand> captor = ArgumentCaptor.forClass(ModifyPendingPointCommand.class);
        verify(modifyPendingPointUseCase, times(2)).modifyPending(captor.capture());

        List<ModifyPendingPointCommand> commands = captor.getAllValues();
        long expectedAmount = Math.round(50000L * 0.1f);

        // 첫 번째 공유자 (200L)
        ModifyPendingPointCommand firstCommand = commands.get(0);
        assertThat(firstCommand.userId()).isEqualTo(200L);
        assertThat(firstCommand.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(firstCommand.amount()).isEqualTo(expectedAmount);
        assertThat(firstCommand.sourceType()).isEqualTo(UserPointSourceType.ORDER);
        assertThat(firstCommand.sourceId()).isEqualTo(1L);
        assertThat(firstCommand.reason()).isEqualTo("링크 공유 회원 상품 구매");

        // 두 번째 공유자 (300L)
        ModifyPendingPointCommand secondCommand = commands.get(1);
        assertThat(secondCommand.userId()).isEqualTo(300L);
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
                new OrderProductItem(1L, 200L, 2, 10000L),
                new OrderProductItem(2L, 200L, 1, 20000L),
                new OrderProductItem(3L, 300L, 1, 15000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(modifyPendingPointUseCase, times(2)).modifyPending(any(ModifyPendingPointCommand.class));
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
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, 200L, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 100L, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullBuyerId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, 200L, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, 50000L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 이벤트 수신 시 DuplicateUserPointHistoryException이 발생하면 멱등 처리하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_duplicateEvent_idempotentAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, 200L, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);
        doThrow(new DuplicateUserPointHistoryException(200L, UserPointSourceType.ORDER, 1L, "링크 공유 회원 상품 구매"))
                .when(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("공유자 2명 중 첫 번째만 중복이면 두 번째는 정상 적립하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_partialDuplicate_continuesAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, 200L, 2, 10000L),
                new OrderProductItem(2L, 300L, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);
        when(modifyPendingPointUseCase.modifyPending(any(ModifyPendingPointCommand.class)))
                .thenThrow(new DuplicateUserPointHistoryException(200L, UserPointSourceType.ORDER, 1L, "링크 공유 회원 상품 구매"))
                .thenReturn(null);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(modifyPendingPointUseCase, times(2)).modifyPending(any(ModifyPendingPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("공유자 전원 중복이면 모두 멱등 처리하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_allDuplicate_idempotentAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, 200L, 2, 10000L),
                new OrderProductItem(2L, 300L, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);
        when(modifyPendingPointUseCase.modifyPending(any(ModifyPendingPointCommand.class)))
                .thenThrow(new DuplicateUserPointHistoryException(200L, UserPointSourceType.ORDER, 1L, "링크 공유 회원 상품 구매"))
                .thenThrow(new DuplicateUserPointHistoryException(300L, UserPointSourceType.ORDER, 1L, "링크 공유 회원 상품 구매"));

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(modifyPendingPointUseCase, times(2)).modifyPending(any(ModifyPendingPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handleOrderPaymentCompletedEvent_unexpectedException_propagatesWithoutAcknowledge() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, 200L, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, orderProducts);
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderPaymentCompletedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 실패");

        verify(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));
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
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("totalAmount가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullTotalAmount_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, 200L, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, null, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("totalAmount가 0이면 적립 금액이 0이므로 적립을 생략하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_zeroTotalAmount_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, 200L, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 0L, orderProducts);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }
}
