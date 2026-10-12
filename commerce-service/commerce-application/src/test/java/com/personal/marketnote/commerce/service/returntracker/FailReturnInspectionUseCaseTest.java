package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnRefundStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTrackerSnapshotState;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
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
@DisplayName("FailReturnInspectionUseCase 테스트")
class FailReturnInspectionUseCaseTest {

    @InjectMocks
    private FailReturnInspectionService service;

    @Mock
    private UpdateReturnTrackerPort updateReturnTrackerPort;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private UpdateOrderPort updateOrderPort;

    @Mock
    private PublishOrderEventPort publishOrderEventPort;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 16, 10, 0);

    @Nested
    @DisplayName("검수 불합격 처리 성공")
    class InspectionFailedSuccess {

        @Test
        @DisplayName("검수 불합격 시 주문 상태가 RETURN_REJECTED로 전이된다")
        void shouldTransitionOrderStatusToReturnRejected() {
            // given
            ReturnTracker tracker = createPendingTracker(1L, 100L);

            Order order = mock(Order.class);
            when(order.getId()).thenReturn(100L);
            when(order.getBuyerId()).thenReturn(200L);
            when(order.getOrderStatus()).thenReturn(OrderStatus.RETURN_IN_PROGRESS);
            when(getOrderUseCase.getOrder(100L)).thenReturn(order);

            // when
            service.failInspection(tracker, NOW);

            // then
            verify(order).changeAllProductsStatus(eq(OrderStatus.RETURN_REJECTED), eq(NOW));
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("검수 불합격 시 ReturnTracker 검수 상태가 FAILED로 전이된다")
        void shouldTransitionTrackerInspectionStatusToFailed() {
            // given
            ReturnTracker tracker = createPendingTracker(1L, 100L);

            Order order = mock(Order.class);
            when(order.getId()).thenReturn(100L);
            when(order.getBuyerId()).thenReturn(200L);
            when(order.getOrderStatus()).thenReturn(OrderStatus.RETURN_IN_PROGRESS);
            when(getOrderUseCase.getOrder(100L)).thenReturn(order);

            // when
            service.failInspection(tracker, NOW);

            // then
            assertThat(tracker.isInspectionFailed()).isTrue();
            assertThat(tracker.getInspectedAt()).isEqualTo(NOW);
            verify(updateReturnTrackerPort).update(tracker);
        }

        @Test
        @DisplayName("검수 불합격 이벤트 발행 시 고객 알림이 전송된다")
        void shouldPublishReturnRejectedEvent() {
            // given
            ReturnTracker tracker = createPendingTracker(1L, 100L);

            Order order = mock(Order.class);
            when(order.getId()).thenReturn(100L);
            when(order.getBuyerId()).thenReturn(200L);
            when(order.getOrderStatus()).thenReturn(OrderStatus.RETURN_IN_PROGRESS);
            when(getOrderUseCase.getOrder(100L)).thenReturn(order);

            // when
            service.failInspection(tracker, NOW);

            // then
            verify(publishOrderEventPort).publishReturnRejectedEvent(100L, 200L);
        }
    }

    @Nested
    @DisplayName("검수 불합격 처리 - 전이 불가")
    class InspectionFailedNonTransitionable {

        @Test
        @DisplayName("검수 보류 시 주문 상태가 RETURN_IN_PROGRESS를 유지한다")
        void shouldNotChangeOrderStatusWhenTransitionNotAllowed() {
            // given
            ReturnTracker tracker = createPendingTracker(1L, 100L);

            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.CANCELLED);
            when(getOrderUseCase.getOrder(100L)).thenReturn(order);

            // when
            service.failInspection(tracker, NOW);

            // then
            verify(order, never()).changeAllProductsStatus(any(), any());
            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    private ReturnTracker createPendingTracker(Long id, Long orderId) {
        return ReturnTracker.from(ReturnTrackerSnapshotState.builder()
                .id(id)
                .orderId(orderId)
                .returnSlipNumber("RS-001")
                .inspectionStatus(ReturnInspectionStatus.PENDING)
                .refundStatus(ReturnRefundStatus.PENDING)
                .build());
    }
}
