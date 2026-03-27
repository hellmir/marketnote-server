package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderStatusChangeException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeOrderStatusRoleValidationUseCaseTest {
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
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private ChangeOrderStatusService changeOrderStatusService;

    // ==================================================================================
    // 구매자 역할 - 허용 상태 변경 테스트
    // ==================================================================================

    @Nested
    @DisplayName("구매자 역할 - 허용 상태 변경")
    class BuyerAllowedStatusChangeTest {

        @Test
        @DisplayName("구매자가 CANCEL_REQUESTED로 변경하면 정상 처리된다")
        void buyer_cancelRequested_succeeds() {
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .role("BUYER")
                    .buyerId(1L)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("구매자가 CONFIRMED로 변경하면 정상 처리된다")
        void buyer_confirmed_succeeds() {
            Order order = createOrder(1L, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .role("BUYER")
                    .buyerId(1L)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("구매자가 EXCHANGE_REQUESTED로 변경하면 정상 처리된다")
        void buyer_exchangeRequested_succeeds() {
            Order order = createOrder(1L, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.EXCHANGE_REQUESTED)
                    .role("BUYER")
                    .buyerId(1L)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("구매자가 REFUND_REQUESTED로 변경하면 정상 처리된다")
        void buyer_refundRequested_succeeds() {
            Order order = createOrder(1L, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.REFUND_REQUESTED)
                    .role("BUYER")
                    .buyerId(1L)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }
    }

    // ==================================================================================
    // 구매자 역할 - 차단 상태 변경 테스트
    // ==================================================================================

    @Nested
    @DisplayName("구매자 역할 - 차단 상태 변경")
    class BuyerBlockedStatusChangeTest {

        @Test
        @DisplayName("구매자가 PAID로 변경하면 UnauthorizedOrderStatusChangeException이 발생한다")
        void buyer_paid_throwsException() {
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PAID)
                    .role("BUYER")
                    .buyerId(1L)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(UnauthorizedOrderStatusChangeException.class);
        }

        @Test
        @DisplayName("구매자가 PREPARING으로 변경하면 UnauthorizedOrderStatusChangeException이 발생한다")
        void buyer_preparing_throwsException() {
            Order order = createOrder(1L, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PREPARING)
                    .role("BUYER")
                    .buyerId(1L)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(UnauthorizedOrderStatusChangeException.class);
        }

        @Test
        @DisplayName("구매자가 SHIPPING으로 변경하면 UnauthorizedOrderStatusChangeException이 발생한다")
        void buyer_shipping_throwsException() {
            Order order = createOrder(1L, OrderStatus.PREPARED);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.SHIPPING)
                    .role("BUYER")
                    .buyerId(1L)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(UnauthorizedOrderStatusChangeException.class);
        }

        @Test
        @DisplayName("구매자가 DELIVERED로 변경하면 UnauthorizedOrderStatusChangeException이 발생한다")
        void buyer_delivered_throwsException() {
            Order order = createOrder(1L, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.DELIVERED)
                    .role("BUYER")
                    .buyerId(1L)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(UnauthorizedOrderStatusChangeException.class);
        }

        @Test
        @DisplayName("구매자가 CANCELLED로 변경하면 UnauthorizedOrderStatusChangeException이 발생한다")
        void buyer_cancelled_throwsException() {
            Order order = createOrder(1L, OrderStatus.CANCEL_REQUESTED);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.CANCELLED)
                    .role("BUYER")
                    .buyerId(1L)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(UnauthorizedOrderStatusChangeException.class);
        }

        @Test
        @DisplayName("구매자가 차단된 상태 변경 시 UpdateOrderPort를 호출하지 않는다")
        void buyer_blockedStatus_doesNotCallUpdatePort() {
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PAID)
                    .role("BUYER")
                    .buyerId(1L)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(UnauthorizedOrderStatusChangeException.class);

            verifyNoInteractions(updateOrderPort);
        }
    }

    // ==================================================================================
    // 서비스 내부 호출 (role == null) - 모든 상태 변경 허용
    // ==================================================================================

    @Nested
    @DisplayName("서비스 내부 호출 (role == null) - 모든 상태 변경 허용")
    class InternalCallTest {

        @Test
        @DisplayName("서비스 내부에서 PAID로 변경하면 정상 처리된다")
        void internalCall_paid_succeeds() {
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("서비스 내부에서 FAILED로 변경하면 정상 처리된다")
        void internalCall_failed_succeeds() {
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.FAILED)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("서비스 내부에서 PREPARING으로 변경하면 정상 처리된다")
        void internalCall_preparing_succeeds() {
            Order order = createOrder(1L, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PREPARING)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }
    }

    // ==================================================================================
    // 관리자/판매자 역할 - 모든 상태 변경 허용
    // ==================================================================================

    @Nested
    @DisplayName("관리자/판매자 역할 - 모든 상태 변경 허용")
    class AdminSellerTest {

        @Test
        @DisplayName("ADMIN이 PAID로 변경하면 정상 처리된다")
        void admin_paid_succeeds() {
            Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PAID)
                    .role("ADMIN")
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("SELLER가 PREPARING으로 변경하면 정상 처리된다")
        void seller_preparing_succeeds() {
            Order order = createOrder(1L, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PREPARING)
                    .role("SELLER")
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }
    }

    // ==================================================================================
    // 구매자 역할 - 소유자 검증 (IDOR 방어)
    // ==================================================================================

    @Nested
    @DisplayName("구매자 역할 - 소유자 검증 (IDOR 방어)")
    class BuyerOwnerVerificationTest {

        @Test
        @DisplayName("구매자가 자신의 주문 상태를 변경하면 정상 처리된다")
        void buyer_ownOrder_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("구매자가 타인의 주문 상태를 변경하면 UnauthorizedOrderAccessException이 발생한다")
        void buyer_otherOrder_throwsException() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .role("BUYER")
                    .buyerId(attackerBuyerId)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }

        @Test
        @DisplayName("구매자가 타인의 주문 상태 변경 시 UpdateOrderPort를 호출하지 않는다")
        void buyer_otherOrder_doesNotCallUpdatePort() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .role("BUYER")
                    .buyerId(attackerBuyerId)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verifyNoInteractions(updateOrderPort);
        }

        @Test
        @DisplayName("서비스 내부 호출(isInternalCall)이면 소유자 검증을 스킵한다")
        void internalCall_skipsOwnerVerification() {
            Long orderId = 1L;
            Order order = createOrderWithBuyerId(orderId, 100L, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("ADMIN 역할이면 소유자 검증을 수행하지 않는다")
        void admin_skipsOwnerVerification() {
            Long orderId = 1L;
            Order order = createOrderWithBuyerId(orderId, 100L, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .role("ADMIN")
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("SELLER 역할이면 소유자 검증을 수행하지 않는다")
        void seller_skipsOwnerVerification() {
            Long orderId = 1L;
            Order order = createOrderWithBuyerId(orderId, 100L, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PREPARING)
                    .role("SELLER")
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrderWithBuyerId(Long orderId, Long buyerId, OrderStatus status) {
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
                .buyerId(buyerId)
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
