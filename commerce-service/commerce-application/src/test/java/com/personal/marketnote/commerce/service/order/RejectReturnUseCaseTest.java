package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.RejectReturnCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RejectReturnUseCaseTest {
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private PublishOrderEventPort publishOrderEventPort;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-09-03T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private RejectReturnService rejectReturnService;

    // ==================================================================================
    // 정상 반품 불가 판정
    // ==================================================================================

    @Nested
    @DisplayName("정상 반품 불가 판정")
    class SuccessfulRejectReturnTest {

        @Test
        @DisplayName("반품 요청 상태의 주문을 반품 불가 판정하면 정상 처리된다")
        void rejectReturn_fromReturnRequested_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.RETURN_REQUESTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RejectReturnCommand command = RejectReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("검수 결과 상품 하자 없음")
                    .build();

            assertThatCode(() -> rejectReturnService.rejectReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("반품 불가 판정 성공 시 UpdateOrderPort를 호출한다")
        void rejectReturn_callsUpdateOrderPort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.RETURN_REQUESTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RejectReturnCommand command = RejectReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("검수 결과 상품 하자 없음")
                    .build();

            rejectReturnService.rejectReturn(command);

            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("반품 불가 판정 성공 시 ReturnRejectedEvent를 발행한다")
        void rejectReturn_publishesReturnRejectedEvent() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.RETURN_REQUESTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RejectReturnCommand command = RejectReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("검수 결과 상품 하자 없음")
                    .build();

            rejectReturnService.rejectReturn(command);

            verify(publishOrderEventPort).publishReturnRejectedEvent(orderId, buyerId);
        }

        @Test
        @DisplayName("반품 불가 판정 성공 시 사유 카테고리와 사유가 OrderStatusHistory에 포함된다")
        void rejectReturn_includesReasonInHistory() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.RETURN_REQUESTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RejectReturnCommand command = RejectReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("검수 결과 상품 하자 없음")
                    .build();

            rejectReturnService.rejectReturn(command);

            ArgumentCaptor<OrderStatusHistory> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistory.class);
            verify(updateOrderPort).update(eq(order), historyCaptor.capture());

            OrderStatusHistory captured = historyCaptor.getValue();
            assertThat(captured.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REJECTED);
            assertThat(captured.getReasonCategory()).isEqualTo(OrderStatusReasonCategory.ETC);
            assertThat(captured.getReason()).isEqualTo("검수 결과 상품 하자 없음");
        }
    }

    // ==================================================================================
    // 상태 전이 검증
    // ==================================================================================

    @Nested
    @DisplayName("상태 전이 검증")
    class StatusTransitionValidationTest {

        @Test
        @DisplayName("이미 반품 불가인 주문을 반품 불가 판정하면 OrderStatusAlreadyChangedException이 발생한다")
        void rejectReturn_alreadyRejected_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.RETURN_REJECTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RejectReturnCommand command = RejectReturnCommand.builder()
                    .id(orderId)
                    .build();

            assertThatThrownBy(() -> rejectReturnService.rejectReturn(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);
        }

        @Test
        @DisplayName("배송 완료 상태의 주문을 반품 불가 판정하면 InvalidOrderStatusTransitionException이 발생한다")
        void rejectReturn_fromDelivered_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RejectReturnCommand command = RejectReturnCommand.builder()
                    .id(orderId)
                    .build();

            assertThatThrownBy(() -> rejectReturnService.rejectReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("반품 진행 중 상태의 주문을 반품 불가 판정하면 InvalidOrderStatusTransitionException이 발생한다")
        void rejectReturn_fromReturnInProgress_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.RETURN_IN_PROGRESS);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RejectReturnCommand command = RejectReturnCommand.builder()
                    .id(orderId)
                    .build();

            assertThatThrownBy(() -> rejectReturnService.rejectReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("결제 완료 상태의 주문을 반품 불가 판정하면 InvalidOrderStatusTransitionException이 발생한다")
        void rejectReturn_fromPaid_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RejectReturnCommand command = RejectReturnCommand.builder()
                    .id(orderId)
                    .build();

            assertThatThrownBy(() -> rejectReturnService.rejectReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("상태 전이 실패 시 UpdateOrderPort를 호출하지 않는다")
        void rejectReturn_invalidTransition_doesNotCallUpdatePort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RejectReturnCommand command = RejectReturnCommand.builder()
                    .id(orderId)
                    .build();

            assertThatThrownBy(() -> rejectReturnService.rejectReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
        }

        @Test
        @DisplayName("상태 전이 실패 시 PublishOrderEventPort를 호출하지 않는다")
        void rejectReturn_invalidTransition_doesNotCallPublishPort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.RETURN_IN_PROGRESS);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RejectReturnCommand command = RejectReturnCommand.builder()
                    .id(orderId)
                    .build();

            assertThatThrownBy(() -> rejectReturnService.rejectReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrder(Long orderId, Long buyerId, OrderStatus status) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(100L)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(status)
                        .accumulatedPoint(0L)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(status)
                .amount(OrderAmount.of(50000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
