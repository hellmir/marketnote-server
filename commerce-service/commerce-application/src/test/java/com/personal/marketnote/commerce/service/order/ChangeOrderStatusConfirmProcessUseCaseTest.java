package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.DeleteOrderedCartProductsPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeOrderStatusConfirmProcessUseCaseTest {
    @Mock
    private ReduceProductInventoryUseCase reduceProductInventoryUseCase;
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private DeleteOrderedCartProductsPort deleteOrderedCartProductsPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private ModifyUserPointPort modifyUserPointPort;

    @InjectMocks
    private ChangeOrderStatusService changeOrderStatusService;

    @Nested
    @DisplayName("CONFIRMED 상태 변경 시 적립 예정 포인트 확정")
    class ConfirmPendingPointTest {

        @Test
        @DisplayName("구매 확정 시 적립 예정 포인트 확정을 호출한다")
        void shouldCallConfirmPendingPointsWhenConfirmed() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;

            Order order = createOrder(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort).confirmPendingPoints(buyerId, orderId);
        }

        @Test
        @DisplayName("구매 확정 시 적립 예정 포인트 확정 실패해도 예외가 전파되지 않는다")
        void shouldNotPropagateExceptionWhenConfirmPendingPointsFails() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;

            Order order = createOrder(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            doThrow(new RuntimeException("리워드 서비스 요청 실패"))
                    .when(modifyUserPointPort).confirmPendingPoints(buyerId, orderId);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("부분 구매 확정 시 적립 예정 포인트 확정을 호출하지 않는다")
        void shouldNotCallConfirmPendingPointsWhenPartiallyConfirmed() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;

            Order order = createOrderWithMultipleProducts(orderId, buyerId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .pricePolicyIds(List.of(10L))
                    .orderStatus(OrderStatus.CONFIRMED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort, never()).confirmPendingPoints(anyLong(), anyLong());
        }

        @Test
        @DisplayName("결제 완료 시 적립 예정 포인트 확정을 호출하지 않는다")
        void shouldNotCallConfirmPendingPointsWhenPaid() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId = 10L;

            Order order = createOrder(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of());

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort, never()).confirmPendingPoints(anyLong(), anyLong());
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
                        .pricePolicyId(10L)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(status)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(status)
                .totalAmount(50000L)
                .couponAmount(0L)
                .pointAmount(0L)
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithMultipleProducts(Long orderId, Long buyerId) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(10L)
                        .quantity(1)
                        .unitAmount(30000L)
                        .orderStatus(OrderStatus.DELIVERED)
                        .build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(20L)
                        .quantity(1)
                        .unitAmount(20000L)
                        .orderStatus(OrderStatus.DELIVERED)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.DELIVERED)
                .totalAmount(50000L)
                .couponAmount(0L)
                .pointAmount(0L)
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
