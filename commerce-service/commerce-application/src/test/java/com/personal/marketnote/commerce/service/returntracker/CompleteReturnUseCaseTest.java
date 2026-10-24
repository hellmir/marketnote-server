package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.common.domain.quantity.Quantity;
import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.port.in.command.order.CalculateReturnShippingFeeCommand;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnCommand;
import com.personal.marketnote.commerce.port.in.command.saga.OrderReturnSagaContext;
import com.personal.marketnote.commerce.port.in.result.order.CalculateReturnShippingFeeResult;
import com.personal.marketnote.commerce.port.in.usecase.order.CalculateReturnShippingFeeUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.service.saga.OrderReturnSagaStarter;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteReturnUseCase 테스트")
class CompleteReturnUseCaseTest {

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

    @Spy
    private Optional<OrderReturnSagaStarter> orderReturnSagaStarter = Optional.empty();

    @Nested
    @DisplayName("반품 완료 처리 성공")
    class CompleteReturnSuccess {

        @Test
        @DisplayName("RETURN_REQUESTED 상태의 주문을 RETURNED로 전이하고 OrderReturnedEvent를 발행한다")
        void shouldTransitionFromReturnRequestedToReturnedAndPublishEvent() {
            OrderProduct product = mock(OrderProduct.class);
            Order order = createReturnableOrder(OrderStatus.RETURN_REQUESTED, product);
            when(product.getOrderStatus()).thenReturn(OrderStatus.RETURN_REQUESTED);

            simulateReturnTransition(order, product, OrderStatus.RETURNED);

            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(
                    any(CalculateReturnShippingFeeCommand.class)
            )).thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(3000L).build());

            service.completeReturn(new CompleteReturnCommand(1L));

            verify(order).changeProductsStatus(eq(List.of(10L)), eq(OrderStatus.RETURN_IN_PROGRESS), any(LocalDateTime.class), eq(0L));
            verify(order).changeProductsStatus(eq(List.of(10L)), eq(OrderStatus.RETURNED), any(LocalDateTime.class), eq(0L));
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), anyString(), eq(100L),
                    eq(30000L), eq(49000L), eq(1000L), eq(3000L),
                    eq(true), eq(3000L), anyList(), anyString()
            );
        }

        @Test
        @DisplayName("RETURN_IN_PROGRESS 상태의 주문을 RETURNED로 바로 전이한다")
        void shouldTransitionFromReturnInProgressToReturned() {
            OrderProduct product = mock(OrderProduct.class);
            Order order = createReturnableOrder(OrderStatus.RETURN_IN_PROGRESS, product);
            when(product.getOrderStatus()).thenReturn(OrderStatus.RETURN_IN_PROGRESS);

            simulateReturnTransition(order, product, OrderStatus.RETURNED);

            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(
                    any(CalculateReturnShippingFeeCommand.class)
            )).thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(0L).build());

            service.completeReturn(new CompleteReturnCommand(1L));

            verify(order, times(2)).changeProductsStatus(eq(List.of(10L)), any(OrderStatus.class), any(LocalDateTime.class), eq(0L));
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), anyString(), eq(100L),
                    eq(30000L), eq(49000L), eq(1000L), eq(3000L),
                    eq(true), eq(0L), anyList(), anyString()
            );
        }

        @Test
        @DisplayName("PARTIALLY_RETURN_REQUESTED 상태의 주문을 RETURNED로 전이할 수 있다")
        void shouldAllowCompletionFromPartiallyReturnRequested() {
            OrderProduct product = mock(OrderProduct.class);
            Order order = createReturnableOrder(OrderStatus.PARTIALLY_RETURN_REQUESTED, product);
            when(product.getOrderStatus()).thenReturn(OrderStatus.RETURN_REQUESTED);

            simulateReturnTransition(order, product, OrderStatus.RETURNED);

            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(
                    any(CalculateReturnShippingFeeCommand.class)
            )).thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(0L).build());

            service.completeReturn(new CompleteReturnCommand(1L));

            verify(order).changeProductsStatus(eq(List.of(10L)), eq(OrderStatus.RETURN_IN_PROGRESS), any(LocalDateTime.class), eq(0L));
            verify(order).changeProductsStatus(eq(List.of(10L)), eq(OrderStatus.RETURNED), any(LocalDateTime.class), eq(0L));
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("반품 배송비를 CalculateReturnShippingFeeUseCase로 계산하여 이벤트에 포함한다")
        void shouldCalculateReturnShippingFeeAndIncludeInEvent() {
            OrderProduct product = mock(OrderProduct.class);
            Order order = createReturnableOrder(OrderStatus.RETURN_REQUESTED, product);
            when(product.getOrderStatus()).thenReturn(OrderStatus.RETURN_REQUESTED);

            simulateReturnTransition(order, product, OrderStatus.RETURNED);

            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(
                    any(CalculateReturnShippingFeeCommand.class)
            )).thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(5000L).build());

            service.completeReturn(new CompleteReturnCommand(1L));

            verify(calculateReturnShippingFeeUseCase).calculateReturnShippingFee(
                    argThat(cmd -> cmd.orderId().equals(1L)
                            && cmd.reasonCategory() == OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND
                            && cmd.returnPricePolicyIds().equals(List.of(10L))
                    )
            );
            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), anyString(), eq(100L),
                    eq(30000L), eq(49000L), eq(1000L), eq(3000L),
                    eq(true), eq(5000L), anyList(), anyString()
            );
        }
    }

    @Nested
    @DisplayName("부분 반품 처리")
    class PartialReturnTest {

        @Test
        @DisplayName("전체 반품 완료 시 주문 상태가 RETURNED로 전이된다")
        void shouldTransitionToReturnedWhenAllProductsReturned() {
            OrderProduct product = mock(OrderProduct.class);
            Order order = createReturnableOrder(OrderStatus.RETURN_REQUESTED, product);
            when(product.getOrderStatus()).thenReturn(OrderStatus.RETURN_REQUESTED);

            simulateReturnTransition(order, product, OrderStatus.RETURNED);

            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(
                    any(CalculateReturnShippingFeeCommand.class)
            )).thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(3000L).build());

            service.completeReturn(new CompleteReturnCommand(1L));

            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), anyString(), eq(100L),
                    eq(30000L), eq(49000L), eq(1000L), eq(3000L),
                    eq(true), eq(3000L), anyList(), anyString()
            );
        }

        @Test
        @DisplayName("부분 반품 완료 시 주문 상태가 PARTIALLY_RETURNED로 전이된다")
        void shouldTransitionToPartiallyReturnedWhenPartialReturn() {
            // given
            OrderProduct returnProduct = mock(OrderProduct.class);
            OrderProduct confirmedProduct = mock(OrderProduct.class);

            Order order = mock(Order.class);
            when(order.getId()).thenReturn(1L);
            when(order.getBuyerId()).thenReturn(100L);
            when(order.getOrderKey()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            when(order.getOrderStatus()).thenReturn(OrderStatus.RETURN_REQUESTED);
            when(order.getStatusChangeReasonCategory()).thenReturn(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND);

            OrderAmount amount = OrderAmount.from(OrderAmountCreateState.builder().totalAmount(80000L).paidAmount(79000L).couponAmount(0L).pointAmount(1000L).shippingFee(3000L).build());
            when(order.getAmount()).thenReturn(amount);

            when(returnProduct.getPricePolicyId()).thenReturn(10L);
            when(returnProduct.getOrderStatus()).thenReturn(OrderStatus.RETURN_REQUESTED);
            when(returnProduct.getQuantity()).thenReturn(Quantity.of(2));
            when(returnProduct.getUnitAmount()).thenReturn(Money.of(15000L));

            when(confirmedProduct.getPricePolicyId()).thenReturn(20L);
            when(confirmedProduct.getOrderStatus()).thenReturn(OrderStatus.CONFIRMED);

            when(order.getOrderProducts()).thenReturn(List.of(returnProduct, confirmedProduct));

            lenient().doAnswer(invocation -> {
                when(returnProduct.getOrderStatus()).thenReturn(OrderStatus.RETURNED);
                when(order.getOrderStatus()).thenReturn(OrderStatus.PARTIALLY_RETURNED);
                return null;
            }).when(order).changeProductsStatus(anyList(), eq(OrderStatus.RETURNED), any(LocalDateTime.class), anyLong());

            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(
                    any(CalculateReturnShippingFeeCommand.class)
            )).thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(3000L).build());

            // when
            service.completeReturn(new CompleteReturnCommand(1L));

            // then
            verify(order).changeProductsStatus(eq(List.of(10L)), eq(OrderStatus.RETURN_IN_PROGRESS), any(LocalDateTime.class), eq(0L));
            verify(order).changeProductsStatus(eq(List.of(10L)), eq(OrderStatus.RETURNED), any(LocalDateTime.class), eq(0L));
            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), anyString(), eq(100L),
                    eq(30000L), eq(79000L), eq(1000L), eq(3000L),
                    eq(false), eq(3000L), anyList(), anyString()
            );
        }

        @Test
        @DisplayName("PARTIALLY_RETURNED 상태에서 추가 반품 요청이 가능하다")
        void shouldAllowReturnRequestFromPartiallyReturned() {
            assertThat(OrderStatus.PARTIALLY_RETURNED.canTransitionTo(OrderStatus.RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("모든 상품 반품 완료 시 PARTIALLY_RETURNED에서 RETURNED로 전이된다")
        void shouldTransitionToReturnedFromPartiallyReturnedWhenAllProductsReturned() {
            // given
            OrderProduct alreadyReturnedProduct = mock(OrderProduct.class);
            OrderProduct newReturnProduct = mock(OrderProduct.class);

            Order order = mock(Order.class);
            when(order.getId()).thenReturn(1L);
            when(order.getBuyerId()).thenReturn(100L);
            when(order.getOrderKey()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            when(order.getOrderStatus()).thenReturn(OrderStatus.PARTIALLY_RETURNED);
            when(order.getStatusChangeReasonCategory()).thenReturn(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND);

            OrderAmount amount = OrderAmount.from(OrderAmountCreateState.builder().totalAmount(80000L).paidAmount(79000L).couponAmount(0L).pointAmount(1000L).shippingFee(3000L).build());
            when(order.getAmount()).thenReturn(amount);

            when(alreadyReturnedProduct.getPricePolicyId()).thenReturn(10L);
            when(alreadyReturnedProduct.getOrderStatus()).thenReturn(OrderStatus.RETURNED);

            when(newReturnProduct.getPricePolicyId()).thenReturn(20L);
            when(newReturnProduct.getOrderStatus()).thenReturn(OrderStatus.RETURN_REQUESTED);
            when(newReturnProduct.getQuantity()).thenReturn(Quantity.of(1));
            when(newReturnProduct.getUnitAmount()).thenReturn(Money.of(20000L));

            when(order.getOrderProducts()).thenReturn(List.of(alreadyReturnedProduct, newReturnProduct));

            lenient().doAnswer(invocation -> {
                when(newReturnProduct.getOrderStatus()).thenReturn(OrderStatus.RETURNED);
                when(order.getOrderStatus()).thenReturn(OrderStatus.RETURNED);
                return null;
            }).when(order).changeProductsStatus(anyList(), eq(OrderStatus.RETURNED), any(LocalDateTime.class), anyLong());

            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(
                    any(CalculateReturnShippingFeeCommand.class)
            )).thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(3000L).build());

            // when
            service.completeReturn(new CompleteReturnCommand(1L));

            // then
            verify(order).changeProductsStatus(eq(List.of(20L)), eq(OrderStatus.RETURN_IN_PROGRESS), any(LocalDateTime.class), eq(0L));
            verify(order).changeProductsStatus(eq(List.of(20L)), eq(OrderStatus.RETURNED), any(LocalDateTime.class), eq(0L));
            verify(publishOrderEventPort).publishOrderReturnedEvent(
                    eq(1L), anyString(), eq(100L),
                    eq(20000L), eq(79000L), eq(1000L), eq(3000L),
                    eq(true), eq(3000L), anyList(), anyString()
            );
        }
    }

    @Nested
    @DisplayName("SAGA 경로 처리")
    class SagaPath {

        @Test
        @DisplayName("SAGA 시작 시 생성된 returnId가 OrderReturnSagaContext에 포함된다")
        void shouldIncludeReturnIdInSagaContext() {
            OrderReturnSagaStarter sagaStarter = mock(OrderReturnSagaStarter.class);
            orderReturnSagaStarter = Optional.of(sagaStarter);
            CompleteReturnService sagaService = new CompleteReturnService(
                    getOrderUseCase, updateOrderPort, publishOrderEventPort,
                    calculateReturnShippingFeeUseCase, clock, orderReturnSagaStarter
            );

            OrderProduct product = mock(OrderProduct.class);
            Order order = createReturnableOrder(OrderStatus.RETURN_REQUESTED, product);
            when(product.getOrderStatus()).thenReturn(OrderStatus.RETURN_REQUESTED);

            simulateReturnTransition(order, product, OrderStatus.RETURNED);

            when(getOrderUseCase.getOrder(1L)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(
                    any(CalculateReturnShippingFeeCommand.class)
            )).thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(3000L).build());

            sagaService.completeReturn(new CompleteReturnCommand(1L));

            ArgumentCaptor<OrderReturnSagaContext> contextCaptor =
                    ArgumentCaptor.forClass(OrderReturnSagaContext.class);
            verify(sagaStarter).start(contextCaptor.capture());

            OrderReturnSagaContext capturedContext = contextCaptor.getValue();
            assertThat(capturedContext.returnId()).isNotBlank();
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    @Nested
    @DisplayName("반품 완료 처리 실패")
    class CompleteReturnFailure {

        @Test
        @DisplayName("RETURN_REQUESTED/RETURN_IN_PROGRESS가 아닌 상태이면 InvalidOrderStatusTransitionException을 던진다")
        void shouldThrowOnInvalidStatus() {
            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.PAID);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            assertThatThrownBy(() -> service.completeReturn(new CompleteReturnCommand(1L)))
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

            service.completeReturn(new CompleteReturnCommand(1L));

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
            verifyNoInteractions(calculateReturnShippingFeeUseCase);
        }
    }

    private Order createReturnableOrder(OrderStatus status, OrderProduct product) {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(1L);
        when(order.getBuyerId()).thenReturn(100L);
        when(order.getOrderKey()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        when(order.getOrderStatus()).thenReturn(status);
        when(order.getStatusChangeReasonCategory()).thenReturn(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND);

        OrderAmount amount = OrderAmount.from(OrderAmountCreateState.builder().totalAmount(50000L).paidAmount(49000L).couponAmount(0L).pointAmount(1000L).shippingFee(3000L).build());
        when(order.getAmount()).thenReturn(amount);

        when(product.getPricePolicyId()).thenReturn(10L);
        when(product.getQuantity()).thenReturn(Quantity.of(3));
        when(product.getUnitAmount()).thenReturn(Money.of(10000L));
        when(order.getOrderProducts()).thenReturn(List.of(product));

        return order;
    }

    private void simulateReturnTransition(Order order, OrderProduct product, OrderStatus targetStatus) {
        lenient().doAnswer(invocation -> {
            when(product.getOrderStatus()).thenReturn(targetStatus);
            when(order.getOrderStatus()).thenReturn(targetStatus);
            return null;
        }).when(order).changeProductsStatus(anyList(), eq(targetStatus), any(LocalDateTime.class), anyLong());
    }
}
