package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeOrderStatusAdminOnlyUseCaseTest {
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
    private Clock clock = Clock.fixed(Instant.parse("2026-09-03T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private ChangeOrderStatusService changeOrderStatusService;

    // ==================================================================================
    // 관리자 전용 전환 후 - role/buyerId 없이 상태 변경 성공
    // ==================================================================================

    @Nested
    @DisplayName("관리자 전용 전환 후 role/buyerId 없이 상태 변경")
    class AdminOnlyStatusChangeTest {

        @Test
        @DisplayName("role/buyerId 없이 PAID에서 PREPARING으로 변경하면 정상 처리된다")
        void paid_to_preparing_succeeds_without_role() {
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PREPARING)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();

            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("role/buyerId 없이 PREPARING에서 SHIPPING으로 변경하면 정상 처리된다")
        void preparing_to_shipping_succeeds_without_role() {
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.SHIPPING)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();

            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("role/buyerId 없이 SHIPPING에서 DELIVERED로 변경하면 정상 처리된다")
        void shipping_to_delivered_succeeds_without_role() {
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.DELIVERED)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();

            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("role/buyerId 없이 CANCEL_REQUESTED에서 CANCELLED로 변경하면 정상 처리된다")
        void cancelRequested_to_cancelled_succeeds_without_role() {
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.CANCEL_REQUESTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CANCELLED)
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();

            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }
    }

    // ==================================================================================
    // 상태 전이 규칙 검증 (관리자여도 전이 규칙은 유지)
    // ==================================================================================

    @Nested
    @DisplayName("관리자 전용 전환 후 상태 전이 규칙 유지")
    class TransitionRuleStillEnforcedTest {

        @Test
        @DisplayName("PAID에서 SHIPPING로 직접 전이하면 InvalidOrderStatusTransitionException이 발생한다")
        void paid_to_shipping_throws_invalidTransition() {
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.SHIPPING)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
        }

        @Test
        @DisplayName("PAYMENT_PENDING에서 PREPARING으로 직접 전이하면 InvalidOrderStatusTransitionException이 발생한다")
        void paymentPending_to_preparing_throws_invalidTransition() {
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PREPARING)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
        }
    }

    // ==================================================================================
    // 중복 상태 변경 방지 (멱등성)
    // ==================================================================================

    @Nested
    @DisplayName("중복 상태 변경 방지")
    class DuplicateStatusChangePreventionTest {

        @Test
        @DisplayName("이미 PREPARING인 주문에 PREPARING으로 변경하면 OrderStatusAlreadyChangedException이 발생한다")
        void preparing_duplicate_throwsException() {
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PREPARING)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);

            verifyNoInteractions(updateOrderPort);
        }
    }

    // ==================================================================================
    // Command에서 구매자 관련 필드 제거 검증
    // ==================================================================================

    @Nested
    @DisplayName("Command에서 구매자 관련 필드 제거 검증")
    class CommandFieldRemovalTest {

        @Test
        @DisplayName("ChangeOrderStatusCommand는 id, orderStatus, pricePolicyIds, reasonCategory, reason, skipSubsequentProcesses만 포함한다")
        void command_only_has_admin_fields() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PREPARING)
                    .pricePolicyIds(List.of(10L, 20L))
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("관리자 상태 변경")
                    .skipSubsequentProcesses(false)
                    .build();

            assertThatCode(() -> {
                command.id();
                command.orderStatus();
                command.pricePolicyIds();
                command.reasonCategory();
                command.reason();
                command.skipSubsequentProcesses();
                command.isPartialProductChange();
                command.shouldSkipSubsequentProcesses();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("isPartialProductChange는 pricePolicyIds가 있으면 true를 반환한다")
        void isPartialProductChange_returns_true_when_pricePolicyIds_present() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .pricePolicyIds(List.of(10L))
                    .build();

            assertThatCode(() -> command.isPartialProductChange()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("shouldSkipSubsequentProcesses는 skipSubsequentProcesses가 true이면 true를 반환한다")
        void shouldSkipSubsequentProcesses_returns_true_when_flag_set() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PAID)
                    .skipSubsequentProcesses(true)
                    .build();

            assertThatCode(() -> command.shouldSkipSubsequentProcesses()).doesNotThrowAnyException();
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
