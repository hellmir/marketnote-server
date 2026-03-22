package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderStatusHistoryResult;
import com.personal.marketnote.commerce.port.in.result.order.OrderStatusHistoryItem;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderStatusHistoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrderStatusHistoryUseCase 테스트")
class GetOrderStatusHistoryUseCaseTest {

    @InjectMocks
    private GetOrderStatusHistoryService getOrderStatusHistoryService;

    @Mock
    private FindOrderPort findOrderPort;

    @Mock
    private FindOrderStatusHistoryPort findOrderStatusHistoryPort;

    private Order createOrder(Long id) {
        return Order.from(OrderSnapshotState.builder()
                .id(id)
                .buyerId(100L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD" + id)
                .orderStatus(OrderStatus.PAID)
                .totalAmount(100000L)
                .paidAmount(95000L)
                .couponAmount(3000L)
                .pointAmount(2000L)
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null))
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.of(2026, 2, 20, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 20, 10, 0))
                .build());
    }

    private OrderStatusHistory createHistory(Long id, Long orderId, OrderStatus status,
                                             OrderStatusReasonCategory reasonCategory,
                                             String reason, LocalDateTime createdAt) {
        return OrderStatusHistory.from(OrderStatusHistorySnapshotState.builder()
                .id(id)
                .orderId(orderId)
                .orderStatus(status)
                .reasonCategory(reasonCategory)
                .reason(reason)
                .createdAt(createdAt)
                .build());
    }

    @Test
    @DisplayName("주문이 존재하고 이력이 있으면 이력 목록을 반환한다")
    void shouldReturnHistoriesWhenOrderExistsAndHasHistory() {
        // given
        Long orderId = 1L;
        when(findOrderPort.findById(orderId)).thenReturn(Optional.of(createOrder(orderId)));

        List<OrderStatusHistory> histories = List.of(
                createHistory(1L, orderId, OrderStatus.PAYMENT_PENDING, null, "결제 대기",
                        LocalDateTime.of(2026, 2, 20, 10, 0)),
                createHistory(2L, orderId, OrderStatus.PAID, null, "결제 완료",
                        LocalDateTime.of(2026, 2, 20, 10, 5))
        );
        when(findOrderStatusHistoryPort.findAllByOrderId(orderId)).thenReturn(histories);

        // when
        GetOrderStatusHistoryResult result = getOrderStatusHistoryService.getOrderStatusHistory(orderId);

        // then
        assertThat(result.statusHistory()).hasSize(2);
        assertThat(result.statusHistory().get(0).orderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(result.statusHistory().get(1).orderStatus()).isEqualTo(OrderStatus.PAID);
        verify(findOrderPort).findById(orderId);
        verify(findOrderStatusHistoryPort).findAllByOrderId(orderId);
        verifyNoMoreInteractions(findOrderPort, findOrderStatusHistoryPort);
    }

    @Test
    @DisplayName("반환된 이력의 orderId가 요청한 orderId와 일치한다")
    void shouldReturnCorrectOrderId() {
        // given
        Long orderId = 5L;
        when(findOrderPort.findById(orderId)).thenReturn(Optional.of(createOrder(orderId)));

        List<OrderStatusHistory> histories = List.of(
                createHistory(1L, orderId, OrderStatus.PAID, null, "결제 완료",
                        LocalDateTime.of(2026, 2, 20, 10, 0))
        );
        when(findOrderStatusHistoryPort.findAllByOrderId(orderId)).thenReturn(histories);

        // when
        GetOrderStatusHistoryResult result = getOrderStatusHistoryService.getOrderStatusHistory(orderId);

        // then
        assertThat(result.orderId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("반환된 이력 항목에 orderStatus, reasonCategory, reason, createdAt이 포함된다")
    void shouldIncludeAllFieldsInHistoryItem() {
        // given
        Long orderId = 1L;
        when(findOrderPort.findById(orderId)).thenReturn(Optional.of(createOrder(orderId)));

        OrderStatusHistory history = createHistory(
                3L, orderId, OrderStatus.CANCEL_REQUESTED, OrderStatusReasonCategory.CANCEL_ORDER,
                "구매 의사 취소", LocalDateTime.of(2026, 2, 20, 11, 0));
        when(findOrderStatusHistoryPort.findAllByOrderId(orderId)).thenReturn(List.of(history));

        // when
        GetOrderStatusHistoryResult result = getOrderStatusHistoryService.getOrderStatusHistory(orderId);

        // then
        OrderStatusHistoryItem item = result.statusHistory().get(0);
        assertThat(item.id()).isEqualTo(3L);
        assertThat(item.orderStatus()).isEqualTo(OrderStatus.CANCEL_REQUESTED);
        assertThat(item.reasonCategory()).isEqualTo(OrderStatusReasonCategory.CANCEL_ORDER);
        assertThat(item.reason()).isEqualTo("구매 의사 취소");
        assertThat(item.createdAt()).isEqualTo(LocalDateTime.of(2026, 2, 20, 11, 0));
    }

    @Test
    @DisplayName("반환된 이력 항목에 orderStatusDescription이 포함된다")
    void shouldIncludeOrderStatusDescription() {
        // given
        Long orderId = 1L;
        when(findOrderPort.findById(orderId)).thenReturn(Optional.of(createOrder(orderId)));

        OrderStatusHistory history = createHistory(
                1L, orderId, OrderStatus.PAID, null, "결제 완료",
                LocalDateTime.of(2026, 2, 20, 10, 0));
        when(findOrderStatusHistoryPort.findAllByOrderId(orderId)).thenReturn(List.of(history));

        // when
        GetOrderStatusHistoryResult result = getOrderStatusHistoryService.getOrderStatusHistory(orderId);

        // then
        assertThat(result.statusHistory().get(0).orderStatusDescription()).isEqualTo("결제 완료");
    }

    @Test
    @DisplayName("주문은 존재하지만 이력이 없으면 빈 이력 목록을 반환한다")
    void shouldReturnEmptyWhenNoHistory() {
        // given
        Long orderId = 1L;
        when(findOrderPort.findById(orderId)).thenReturn(Optional.of(createOrder(orderId)));
        when(findOrderStatusHistoryPort.findAllByOrderId(orderId)).thenReturn(List.of());

        // when
        GetOrderStatusHistoryResult result = getOrderStatusHistoryService.getOrderStatusHistory(orderId);

        // then
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.statusHistory()).isEmpty();
        verify(findOrderPort).findById(orderId);
        verify(findOrderStatusHistoryPort).findAllByOrderId(orderId);
        verifyNoMoreInteractions(findOrderPort, findOrderStatusHistoryPort);
    }

    @Test
    @DisplayName("주문이 존재하지 않으면 OrderNotFoundException을 던진다")
    void shouldThrowWhenOrderNotFound() {
        // given
        Long orderId = 999L;
        when(findOrderPort.findById(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getOrderStatusHistoryService.getOrderStatusHistory(orderId))
                .isInstanceOf(OrderNotFoundException.class);
        verify(findOrderPort).findById(orderId);
        verifyNoInteractions(findOrderStatusHistoryPort);
    }

    @Test
    @DisplayName("findOrderPort.findById를 정확히 한 번 호출한다")
    void shouldCallFindOrderPortExactlyOnce() {
        // given
        Long orderId = 1L;
        when(findOrderPort.findById(orderId)).thenReturn(Optional.of(createOrder(orderId)));
        when(findOrderStatusHistoryPort.findAllByOrderId(orderId)).thenReturn(List.of());

        // when
        getOrderStatusHistoryService.getOrderStatusHistory(orderId);

        // then
        verify(findOrderPort, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("findOrderStatusHistoryPort.findAllByOrderId를 정확히 한 번 호출한다")
    void shouldCallFindHistoryPortExactlyOnce() {
        // given
        Long orderId = 1L;
        when(findOrderPort.findById(orderId)).thenReturn(Optional.of(createOrder(orderId)));
        when(findOrderStatusHistoryPort.findAllByOrderId(orderId)).thenReturn(List.of());

        // when
        getOrderStatusHistoryService.getOrderStatusHistory(orderId);

        // then
        verify(findOrderStatusHistoryPort, times(1)).findAllByOrderId(orderId);
    }

    @Test
    @DisplayName("주문이 존재하지 않으면 findOrderStatusHistoryPort를 호출하지 않는다")
    void shouldNotCallHistoryPortWhenOrderNotFound() {
        // given
        Long orderId = 999L;
        when(findOrderPort.findById(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getOrderStatusHistoryService.getOrderStatusHistory(orderId))
                .isInstanceOf(OrderNotFoundException.class);
        verifyNoInteractions(findOrderStatusHistoryPort);
    }
}
