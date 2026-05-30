package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.domain.payment.PaymentMethod;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEventSnapshotState;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.order.CalculateReturnShippingFeeCommand;
import com.personal.marketnote.commerce.port.in.command.order.GetReturnRefundInfoCommand;
import com.personal.marketnote.commerce.port.in.result.order.CalculateReturnShippingFeeResult;
import com.personal.marketnote.commerce.port.in.result.order.GetReturnRefundInfoResult;
import com.personal.marketnote.commerce.port.in.usecase.order.CalculateReturnShippingFeeUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetReturnRefundInfoService 테스트")
class GetReturnRefundInfoUseCaseTest {

    @InjectMocks
    private GetReturnRefundInfoService service;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private CalculateReturnShippingFeeUseCase calculateReturnShippingFeeUseCase;

    @Mock
    private FindPspPaymentEventPort findPspPaymentEventPort;

    private static final Long ORDER_ID = 1L;
    private static final Long BUYER_ID = 100L;
    private static final Long SELLER_ID = 10L;
    private static final Long PRICE_POLICY_ID_1 = 200L;
    private static final Long PRICE_POLICY_ID_2 = 201L;
    private static final UUID ORDER_KEY = UUID.randomUUID();

    @Nested
    @DisplayName("환불 예정 정보 조회 성공")
    class GetReturnRefundInfoSuccess {

        @Test
        @DisplayName("반품 가능 상태(DELIVERED)에서 환불 예정 정보를 정상 조회한다")
        void shouldReturnRefundInfoWhenOrderIsDelivered() {
            // given
            Order order = createOrder(OrderStatus.DELIVERED, 50000L, 0L, 0L);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(any(CalculateReturnShippingFeeCommand.class)))
                    .thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(3000L).build());
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY.toString()))
                    .thenReturn(Optional.of(createPspPaymentEvent("CARD")));

            GetReturnRefundInfoCommand command = GetReturnRefundInfoCommand.builder()
                    .orderId(ORDER_ID)
                    .buyerId(BUYER_ID)
                    .reasonCategory(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND)
                    .returnPricePolicyIds(null)
                    .build();

            // when
            GetReturnRefundInfoResult result = service.getReturnRefundInfo(command);

            // then
            assertThat(result.totalProductAmount()).isEqualTo(50000L);
            assertThat(result.returnShippingFee()).isEqualTo(3000L);
            assertThat(result.refundMethod()).isEqualTo(PaymentMethod.CARD.getDescription());
            assertThat(result.estimatedRefundAmount()).isEqualTo(47000L);
            assertThat(result.estimatedRefundCash()).isZero();
        }

        @Test
        @DisplayName("반품 배송비가 환불 예정 금액에서 차감된다")
        void shouldDeductReturnShippingFeeFromRefundAmount() {
            // given
            Order order = createOrder(OrderStatus.DELIVERED, 30000L, 0L, 0L);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(any(CalculateReturnShippingFeeCommand.class)))
                    .thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(6000L).build());
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY.toString()))
                    .thenReturn(Optional.of(createPspPaymentEvent("CARD")));

            GetReturnRefundInfoCommand command = createCommand(null);

            // when
            GetReturnRefundInfoResult result = service.getReturnRefundInfo(command);

            // then
            assertThat(result.totalProductAmount()).isEqualTo(30000L);
            assertThat(result.returnShippingFee()).isEqualTo(6000L);
            assertThat(result.estimatedRefundAmount()).isEqualTo(24000L);
        }

        @Test
        @DisplayName("포인트 결제 금액이 환불 예정 캐시로 반환된다")
        void shouldReturnPointAmountAsEstimatedRefundCash() {
            // given
            Order order = createOrder(OrderStatus.DELIVERED, 50000L, 0L, 10000L);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(any(CalculateReturnShippingFeeCommand.class)))
                    .thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(3000L).build());
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY.toString()))
                    .thenReturn(Optional.of(createPspPaymentEvent("CARD")));

            GetReturnRefundInfoCommand command = createCommand(null);

            // when
            GetReturnRefundInfoResult result = service.getReturnRefundInfo(command);

            // then
            assertThat(result.estimatedRefundCash()).isEqualTo(10000L);
            assertThat(result.estimatedRefundAmount()).isEqualTo(37000L);
        }

        @Test
        @DisplayName("부분 반품 시 포인트 환불분을 반품 비율에 따라 계산한다")
        void shouldCalculatePointRefundProportionallyForPartialReturn() {
            // given
            Order order = createOrderWithTwoProducts(
                    OrderStatus.DELIVERED,
                    PRICE_POLICY_ID_1, 30000L, 1,
                    PRICE_POLICY_ID_2, 20000L, 1,
                    0L, 10000L
            );
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(any(CalculateReturnShippingFeeCommand.class)))
                    .thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(3000L).build());
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY.toString()))
                    .thenReturn(Optional.of(createPspPaymentEvent("CARD")));

            GetReturnRefundInfoCommand command = createCommand(List.of(PRICE_POLICY_ID_1));

            // when
            GetReturnRefundInfoResult result = service.getReturnRefundInfo(command);

            // then
            // totalProductAmount = 30000 (반품 대상 상품만)
            // pointAmount 비례: 10000 * 30000 / 50000 = 6000
            // estimatedRefundAmount = 30000 - 3000 - 6000 = 21000
            assertThat(result.totalProductAmount()).isEqualTo(30000L);
            assertThat(result.estimatedRefundCash()).isEqualTo(6000L);
            assertThat(result.estimatedRefundAmount()).isEqualTo(21000L);
        }

        @Test
        @DisplayName("PG 환불 예정 금액이 음수이면 0원을 반환한다")
        void shouldReturnZeroWhenEstimatedRefundAmountIsNegative() {
            // given
            Order order = createOrder(OrderStatus.DELIVERED, 5000L, 0L, 3000L);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(any(CalculateReturnShippingFeeCommand.class)))
                    .thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(6000L).build());
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY.toString()))
                    .thenReturn(Optional.of(createPspPaymentEvent("CARD")));

            GetReturnRefundInfoCommand command = createCommand(null);

            // when
            GetReturnRefundInfoResult result = service.getReturnRefundInfo(command);

            // then
            // estimatedRefundAmount = 5000 - 6000 - 3000 = -4000 → 0
            assertThat(result.estimatedRefundAmount()).isZero();
        }
    }

    @Nested
    @DisplayName("환불 예정 정보 조회 실패")
    class GetReturnRefundInfoFailure {

        @Test
        @DisplayName("구매자 소유권 불일치 시 UnauthorizedOrderAccessException이 발생한다")
        void shouldThrowWhenBuyerIdMismatch() {
            // given
            Order order = createOrder(OrderStatus.DELIVERED, 50000L, 0L, 0L);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            Long wrongBuyerId = 999L;
            GetReturnRefundInfoCommand command = GetReturnRefundInfoCommand.builder()
                    .orderId(ORDER_ID)
                    .buyerId(wrongBuyerId)
                    .reasonCategory(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND)
                    .build();

            // when & then
            assertThatThrownBy(() -> service.getReturnRefundInfo(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verifyNoInteractions(calculateReturnShippingFeeUseCase);
            verifyNoInteractions(findPspPaymentEventPort);
        }

        @Test
        @DisplayName("반품 불가 상태에서 조회 시 InvalidOrderStatusTransitionException이 발생한다")
        void shouldThrowWhenOrderStatusIsNotReturnable() {
            // given
            Order order = createOrder(OrderStatus.PAID, 50000L, 0L, 0L);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            GetReturnRefundInfoCommand command = createCommand(null);

            // when & then
            assertThatThrownBy(() -> service.getReturnRefundInfo(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(calculateReturnShippingFeeUseCase);
            verifyNoInteractions(findPspPaymentEventPort);
        }

        @Test
        @DisplayName("reasonCategory가 null이면 ReasonCategoryNoValueException이 발생한다")
        void shouldThrowWhenReasonCategoryIsNull() {
            // given
            Order order = createOrder(OrderStatus.DELIVERED, 50000L, 0L, 0L);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            GetReturnRefundInfoCommand command = GetReturnRefundInfoCommand.builder()
                    .orderId(ORDER_ID)
                    .buyerId(BUYER_ID)
                    .reasonCategory(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> service.getReturnRefundInfo(command))
                    .isInstanceOf(ReasonCategoryNoValueException.class);

            verifyNoInteractions(calculateReturnShippingFeeUseCase);
            verifyNoInteractions(findPspPaymentEventPort);
        }

        @Test
        @DisplayName("취소 전용 사유(CANCEL_ORDER)이면 InvalidReasonCategoryException이 발생한다")
        void shouldThrowWhenCancelOnlyReasonCategory() {
            // given
            Order order = createOrder(OrderStatus.DELIVERED, 50000L, 0L, 0L);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            GetReturnRefundInfoCommand command = GetReturnRefundInfoCommand.builder()
                    .orderId(ORDER_ID)
                    .buyerId(BUYER_ID)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .build();

            // when & then
            assertThatThrownBy(() -> service.getReturnRefundInfo(command))
                    .isInstanceOf(InvalidReasonCategoryException.class);

            verifyNoInteractions(calculateReturnShippingFeeUseCase);
            verifyNoInteractions(findPspPaymentEventPort);
        }

        @Test
        @DisplayName("결제 이벤트를 찾을 수 없으면 PaymentEventNotFoundException이 발생한다")
        void shouldThrowWhenPspPaymentEventNotFound() {
            // given
            Order order = createOrder(OrderStatus.DELIVERED, 50000L, 0L, 0L);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(calculateReturnShippingFeeUseCase.calculateReturnShippingFee(any(CalculateReturnShippingFeeCommand.class)))
                    .thenReturn(CalculateReturnShippingFeeResult.builder().returnShippingFee(3000L).build());
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY.toString()))
                    .thenReturn(Optional.empty());

            GetReturnRefundInfoCommand command = createCommand(null);

            // when & then
            assertThatThrownBy(() -> service.getReturnRefundInfo(command))
                    .isInstanceOf(PaymentEventNotFoundException.class);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private GetReturnRefundInfoCommand createCommand(List<Long> returnPricePolicyIds) {
        return GetReturnRefundInfoCommand.builder()
                .orderId(ORDER_ID)
                .buyerId(BUYER_ID)
                .reasonCategory(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND)
                .returnPricePolicyIds(returnPricePolicyIds)
                .build();
    }

    private Order createOrder(OrderStatus status, long totalProductAmount, long couponAmount, long pointAmount) {
        return Order.from(OrderSnapshotState.builder()
                .id(ORDER_ID)
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY)
                .orderNumber("ORD-" + ORDER_ID)
                .orderStatus(status)
                .amount(OrderAmount.of(totalProductAmount, null, couponAmount, pointAmount, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of(
                        OrderProductSnapshotState.builder()
                                .orderId(ORDER_ID)
                                .sellerId(SELLER_ID)
                                .pricePolicyId(PRICE_POLICY_ID_1)
                                .unitAmount(totalProductAmount)
                                .quantity(1)
                                .orderStatus(status)
                                .build()
                ))
                .build());
    }

    private Order createOrderWithTwoProducts(
            OrderStatus status,
            Long pricePolicyId1, long unitAmount1, int quantity1,
            Long pricePolicyId2, long unitAmount2, int quantity2,
            long couponAmount, long pointAmount
    ) {
        long totalAmount = unitAmount1 * quantity1 + unitAmount2 * quantity2;
        return Order.from(OrderSnapshotState.builder()
                .id(ORDER_ID)
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY)
                .orderNumber("ORD-" + ORDER_ID)
                .orderStatus(status)
                .amount(OrderAmount.of(totalAmount, null, couponAmount, pointAmount, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of(
                        OrderProductSnapshotState.builder()
                                .orderId(ORDER_ID)
                                .sellerId(SELLER_ID)
                                .pricePolicyId(pricePolicyId1)
                                .unitAmount(unitAmount1)
                                .quantity(quantity1)
                                .orderStatus(status)
                                .build(),
                        OrderProductSnapshotState.builder()
                                .orderId(ORDER_ID)
                                .sellerId(SELLER_ID)
                                .pricePolicyId(pricePolicyId2)
                                .unitAmount(unitAmount2)
                                .quantity(quantity2)
                                .orderStatus(status)
                                .build()
                ))
                .build());
    }

    private PspPaymentEvent createPspPaymentEvent(String method) {
        return PspPaymentEvent.from(PspPaymentEventSnapshotState.builder()
                .id(1L)
                .orderId(ORDER_ID)
                .orderKey(ORDER_KEY.toString())
                .method(method)
                .amount(50000L)
                .build());
    }
}
