package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.returntracker.RejectReturnInspectionCommand;
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
@DisplayName("RejectReturnInspectionService 테스트")
class RejectReturnInspectionServiceTest {

    @InjectMocks
    private RejectReturnInspectionService service;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private UpdateOrderPort updateOrderPort;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-03T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    private static final Long ORDER_ID = 100L;

    @Nested
    @DisplayName("반품 불가 판정 성공")
    class RejectSuccess {

        @Test
        @DisplayName("RETURN_INSPECTING 상태의 주문을 RETURN_REJECTED로 전이하고 이력을 저장한다")
        void shouldTransitionToReturnRejectedAndSaveHistory() {
            Order order = createReturnInspectingOrder();
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            service.rejectReturnInspection(new RejectReturnInspectionCommand(ORDER_ID));

            verify(order).changeAllProductsStatus(eq(OrderStatus.RETURN_REJECTED), any());
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }
    }

    @Nested
    @DisplayName("멱등성 처리")
    class Idempotency {

        @Test
        @DisplayName("이미 RETURN_REJECTED 상태이면 아무 처리 없이 정상 종료한다")
        void shouldReturnEarlyWhenAlreadyReturnRejected() {
            Order order = createReturnRejectedOrder();
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            service.rejectReturnInspection(new RejectReturnInspectionCommand(ORDER_ID));

            verifyNoInteractions(updateOrderPort);
        }
    }

    @Nested
    @DisplayName("상태 전이 실패")
    class TransitionFailure {

        @Test
        @DisplayName("RETURN_INSPECTING이 아닌 상태이면 InvalidOrderStatusTransitionException을 던진다")
        void shouldThrowExceptionWhenNotReturnInspecting() {
            Order order = createNonInspectingOrder();
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            assertThatThrownBy(() ->
                    service.rejectReturnInspection(new RejectReturnInspectionCommand(ORDER_ID))
            ).isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
        }
    }

    private Order createReturnInspectingOrder() {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(ORDER_ID);
        when(order.isReturnInspecting()).thenReturn(true);
        return order;
    }

    private Order createReturnRejectedOrder() {
        Order order = mock(Order.class);
        when(order.isReturnRejected()).thenReturn(true);
        return order;
    }

    private Order createNonInspectingOrder() {
        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.RETURN_IN_PROGRESS);
        return order;
    }
}
