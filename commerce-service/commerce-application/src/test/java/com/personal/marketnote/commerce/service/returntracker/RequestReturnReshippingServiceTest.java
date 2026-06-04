package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.returntracker.RequestReturnReshippingCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestReturnReshippingService 테스트")
class RequestReturnReshippingServiceTest {

    @InjectMocks
    private RequestReturnReshippingService service;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private UpdateOrderPort updateOrderPort;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-03T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    private static final Long ORDER_ID = 100L;

    @Nested
    @DisplayName("회송 요청 성공")
    class RequestSuccess {

        @Test
        @DisplayName("RETURN_REJECTED 상태의 주문을 RETURN_RESHIPPING_REQUESTED로 전이하고 이력을 저장한다")
        void shouldTransitionToReturnReshippingRequestedAndSaveHistory() {
            Order order = createReturnRejectedOrder();
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            service.requestReturnReshipping(new RequestReturnReshippingCommand(ORDER_ID));

            verify(order).changeAllProductsStatus(eq(OrderStatus.RETURN_RESHIPPING_REQUESTED), any());
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }
    }

    @Nested
    @DisplayName("멱등성 처리")
    class Idempotency {

        @Test
        @DisplayName("이미 RETURN_RESHIPPING_REQUESTED 상태이면 아무 처리 없이 정상 종료한다")
        void shouldReturnEarlyWhenAlreadyReturnReshippingRequested() {
            Order order = createReturnReshippingRequestedOrder();
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            service.requestReturnReshipping(new RequestReturnReshippingCommand(ORDER_ID));

            verifyNoInteractions(updateOrderPort);
        }
    }

    @Nested
    @DisplayName("상태 전이 실패")
    class TransitionFailure {

        @Test
        @DisplayName("RETURN_REJECTED가 아닌 상태이면 InvalidOrderStatusTransitionException을 던진다")
        void shouldThrowExceptionWhenNotReturnRejected() {
            Order order = createNonRejectedOrder();
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            assertThatThrownBy(() ->
                    service.requestReturnReshipping(new RequestReturnReshippingCommand(ORDER_ID))
            ).isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
        }
    }

    private Order createReturnRejectedOrder() {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(ORDER_ID);
        when(order.isReturnRejected()).thenReturn(true);
        return order;
    }

    private Order createReturnReshippingRequestedOrder() {
        Order order = mock(Order.class);
        when(order.isReturnReshippingRequested()).thenReturn(true);
        return order;
    }

    private Order createNonRejectedOrder() {
        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.RETURN_INSPECTING);
        return order;
    }
}
