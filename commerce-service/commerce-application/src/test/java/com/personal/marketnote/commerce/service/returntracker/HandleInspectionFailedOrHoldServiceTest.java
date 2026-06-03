package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnRefundStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTrackerSnapshotState;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.returntracker.UpdateReturnTrackerPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HandleInspectionFailedOrHoldService 테스트")
class HandleInspectionFailedOrHoldServiceTest {

    @InjectMocks
    private HandleInspectionFailedOrHoldService service;

    @Mock
    private UpdateReturnTrackerPort updateReturnTrackerPort;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private UpdateOrderPort updateOrderPort;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 9, 19, 0);

    @Nested
    @DisplayName("검수 불량 처리")
    class HandleInspectionFailed {

        @Test
        @DisplayName("검수 불량 시 ReturnTracker를 FAILED로 업데이트하고 주문 상태를 RETURN_INSPECTING으로 전이한다")
        void shouldFailInspectionAndTransitionOrderToReturnInspecting() {
            ReturnTracker tracker = createPendingTracker();
            Order order = createReturnInProgressOrder();
            when(getOrderUseCase.getOrder(100L)).thenReturn(order);

            service.handleFailedOrHold(tracker, "02", NOW);

            assertThat(tracker.isInspectionFailed()).isTrue();
            assertThat(tracker.getInspectedAt()).isEqualTo(NOW);
            verify(updateReturnTrackerPort).update(tracker);
            verify(order).changeAllProductsStatus(eq(OrderStatus.RETURN_INSPECTING), eq(NOW));
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }
    }

    @Nested
    @DisplayName("검수 보류 처리")
    class HandleInspectionOnHold {

        @Test
        @DisplayName("검수 보류 시 ReturnTracker를 ON_HOLD로 업데이트하고 주문 상태를 RETURN_INSPECTING으로 전이한다")
        void shouldHoldInspectionAndTransitionOrderToReturnInspecting() {
            ReturnTracker tracker = createPendingTracker();
            Order order = createReturnInProgressOrder();
            when(getOrderUseCase.getOrder(100L)).thenReturn(order);

            service.handleFailedOrHold(tracker, "03", NOW);

            assertThat(tracker.isInspectionOnHold()).isTrue();
            verify(updateReturnTrackerPort).update(tracker);
            verify(order).changeAllProductsStatus(eq(OrderStatus.RETURN_INSPECTING), eq(NOW));
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }
    }

    @Nested
    @DisplayName("멱등성 처리")
    class Idempotency {

        @Test
        @DisplayName("주문이 이미 RETURN_INSPECTING 상태이면 주문 상태 변경 없이 ReturnTracker만 업데이트한다")
        void shouldSkipOrderTransitionWhenAlreadyReturnInspecting() {
            ReturnTracker tracker = createPendingTracker();
            Order order = mock(Order.class);
            when(order.isReturnInspecting()).thenReturn(true);
            when(getOrderUseCase.getOrder(100L)).thenReturn(order);

            service.handleFailedOrHold(tracker, "02", NOW);

            assertThat(tracker.isInspectionFailed()).isTrue();
            verify(updateReturnTrackerPort).update(tracker);
            verify(order, never()).changeAllProductsStatus(any(), any());
            verifyNoInteractions(updateOrderPort);
        }
    }

    private ReturnTracker createPendingTracker() {
        return ReturnTracker.from(ReturnTrackerSnapshotState.builder()
                .id(1L)
                .orderId(100L)
                .returnSlipNumber("RS-001")
                .inspectionStatus(ReturnInspectionStatus.PENDING)
                .refundStatus(ReturnRefundStatus.PENDING)
                .build());
    }

    private Order createReturnInProgressOrder() {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(100L);
        return order;
    }
}
