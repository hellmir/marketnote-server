package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.order.CalculateReturnShippingFeeCommand;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnCommand;
import com.personal.marketnote.commerce.port.in.result.order.CalculateReturnShippingFeeResult;
import com.personal.marketnote.commerce.port.in.usecase.order.CalculateReturnShippingFeeUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteReturnService 테스트")
class CompleteReturnServiceTest {

    @InjectMocks
    private CompleteReturnService service;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private UpdateOrderPort updateOrderPort;

    @Mock
    private PublishOrderEventPort publishOrderEventPort;

    @Mock
    private CalculateReturnShippingFeeUseCase calculateReturnShippingFeeUseCase;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-03T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Nested
    @DisplayName("반품 완료 처리 성공")
    class CompleteReturnSuccess {

        @Test
        @DisplayName("RETURN_REQUESTED 상태의 주문을 RETURNED로 전이하고 OrderReturnedEvent를 발행한다")
        void shouldTransitionFromReturnRequestedToReturnedAndPublishEvent() {
            Order order = createReturnableOrder(OrderStatus.RETURN_REQUESTED);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(any(CalculateReturnShippingFeeCommand.class)))
                    .thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(3000L).build());

            CompleteReturnCommand command = new CompleteReturnCommand(1L);

            service.completeReturn(command);

            verify(order).changeAllProductsStatus(eq(OrderStatus.RETURN_IN_PROGRESS), any());
            verify(order).changeAllProductsStatus(eq(OrderStatus.RETURNED), any());
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), anyString(), eq(100L),
                    eq(30000L), eq(49000L), eq(1000L), eq(3000L),
                    eq(true), eq(3000L), anyList());
        }

        @Test
        @DisplayName("RETURN_INSPECTING 상태의 주문을 RETURNED로 바로 전이한다")
        void shouldTransitionFromReturnInspectingToReturned() {
            Order order = createReturnableOrder(OrderStatus.RETURN_INSPECTING);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(any(CalculateReturnShippingFeeCommand.class)))
                    .thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(0L).build());

            CompleteReturnCommand command = new CompleteReturnCommand(1L);

            service.completeReturn(command);

            verify(order, never()).changeAllProductsStatus(eq(OrderStatus.RETURN_IN_PROGRESS), any());
            verify(order).changeAllProductsStatus(eq(OrderStatus.RETURNED), any());
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), anyString(), eq(100L),
                    eq(30000L), eq(49000L), eq(1000L), eq(3000L),
                    eq(true), eq(0L), anyList());
        }

        @Test
        @DisplayName("RETURN_IN_PROGRESS 상태의 주문을 RETURNED로 바로 전이한다")
        void shouldTransitionFromReturnInProgressToReturned() {
            Order order = createReturnableOrder(OrderStatus.RETURN_IN_PROGRESS);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(any(CalculateReturnShippingFeeCommand.class)))
                    .thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(0L).build());

            CompleteReturnCommand command = new CompleteReturnCommand(1L);

            service.completeReturn(command);

            verify(order, never()).changeAllProductsStatus(eq(OrderStatus.RETURN_IN_PROGRESS), any());
            verify(order).changeAllProductsStatus(eq(OrderStatus.RETURNED), any());
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), anyString(), eq(100L),
                    eq(30000L), eq(49000L), eq(1000L), eq(3000L),
                    eq(true), eq(0L), anyList());
        }

        @Test
        @DisplayName("반품 배송비를 CalculateReturnShippingFeeUseCase로 계산하여 이벤트에 포함한다")
        void shouldCalculateReturnShippingFeeAndIncludeInEvent() {
            Order order = createReturnableOrder(OrderStatus.RETURN_REQUESTED);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(any(CalculateReturnShippingFeeCommand.class)))
                    .thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(5000L).build());

            CompleteReturnCommand command = new CompleteReturnCommand(1L);

            service.completeReturn(command);

            verify(calculateReturnShippingFeeUseCase).calculateReturnShippingFee(
                    argThat(cmd -> cmd.orderId().equals(1L)
                            && cmd.reasonCategory() == OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND));
            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), anyString(), eq(100L),
                    eq(30000L), eq(49000L), eq(1000L), eq(3000L),
                    eq(true), eq(5000L), anyList());
        }
    }

    @Nested
    @DisplayName("반품 완료 처리 실패")
    class CompleteReturnFailure {

        @Test
        @DisplayName("반품 처리 가능한 상태가 아니면 InvalidOrderStatusTransitionException을 던진다")
        void shouldThrowOnInvalidStatus() {
            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.PAID);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            CompleteReturnCommand command = new CompleteReturnCommand(1L);

            assertThatThrownBy(() -> service.completeReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
            verifyNoInteractions(calculateReturnShippingFeeUseCase);
        }
    }

    @Nested
    @DisplayName("멱등성 처리")
    class Idempotency {

        @Test
        @DisplayName("이미 RETURNED 상태인 주문이면 아무 처리 없이 정상 종료한다")
        void shouldSkipAlreadyReturnedOrder() {
            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.RETURNED);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            CompleteReturnCommand command = new CompleteReturnCommand(1L);

            service.completeReturn(command);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
            verifyNoInteractions(calculateReturnShippingFeeUseCase);
        }
    }

    private Order createReturnableOrder(OrderStatus status) {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(1L);
        when(order.getBuyerId()).thenReturn(100L);
        when(order.getOrderKey()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        when(order.getOrderStatus()).thenReturn(status);
        when(order.getStatusChangeReasonCategory()).thenReturn(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND);

        OrderAmount amount = OrderAmount.of(50000L, 49000L, 0L, 1000L, 3000L);
        when(order.getAmount()).thenReturn(amount);

        OrderProduct product = mock(OrderProduct.class);
        when(product.getPricePolicyId()).thenReturn(10L);
        when(product.getQuantity()).thenReturn(3);
        when(product.getUnitAmount()).thenReturn(10000L);
        when(product.getOrderStatus()).thenReturn(OrderStatus.RETURNED);
        when(order.getOrderProducts()).thenReturn(List.of(product));

        return order;
    }
}
