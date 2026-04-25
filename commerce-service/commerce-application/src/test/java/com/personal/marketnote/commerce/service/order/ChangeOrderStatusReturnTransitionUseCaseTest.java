package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.commerce.port.out.user.FindUserShippingAddressPort;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeOrderStatusReturnTransitionUseCaseTest {
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private ModifyUserPointPort modifyUserPointPort;
    @Mock
    private PublishOrderEventPort publishOrderEventPort;
    @Mock
    private FindUserShippingAddressPort findUserShippingAddressPort;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-24T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private ChangeOrderStatusService changeOrderStatusService;

    // ==================================================================================
    // 반품 단일 단계 전이 성공
    // ==================================================================================

    @Nested
    @DisplayName("반품 단일 단계 전이 성공")
    class ReturnSingleStepTransitionTest {

        @Test
        @DisplayName("RETURN_REQUESTED에서 RETURN_IN_PROGRESS로 전이하면 정상 처리된다")
        void returnRequested_to_returnInProgress_succeeds() {
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.RETURN_REQUESTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.RETURN_IN_PROGRESS)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();

            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("RETURN_IN_PROGRESS에서 RETURNED로 전이하면 정상 처리된다")
        void returnInProgress_to_returned_succeeds() {
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.RETURN_IN_PROGRESS);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.RETURNED)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();

            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }
    }

    // ==================================================================================
    // 반품 중복 상태 변경 방지
    // ==================================================================================

    @Nested
    @DisplayName("반품 중복 상태 변경 방지")
    class ReturnDuplicateStatusChangeTest {

        @Test
        @DisplayName("이미 RETURN_IN_PROGRESS인 주문에 RETURN_IN_PROGRESS로 변경하면 OrderStatusAlreadyChangedException이 발생한다")
        void returnInProgress_duplicate_throwsException() {
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.RETURN_IN_PROGRESS);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.RETURN_IN_PROGRESS)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);

            verifyNoInteractions(updateOrderPort);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrder(Long orderId, OrderStatus status) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(100L)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(status)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(status)
                .amount(OrderAmount.of(50000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
