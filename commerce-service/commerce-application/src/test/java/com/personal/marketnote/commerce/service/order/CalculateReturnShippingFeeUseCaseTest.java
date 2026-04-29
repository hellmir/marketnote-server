package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationSnapshotState;
import com.personal.marketnote.commerce.exception.InvalidReasonCategoryException;
import com.personal.marketnote.commerce.exception.ReasonCategoryNoValueException;
import com.personal.marketnote.commerce.port.in.command.order.CalculateReturnShippingFeeCommand;
import com.personal.marketnote.commerce.port.in.result.order.CalculateReturnShippingFeeResult;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.result.shipping.ShippingPolicyInfoResult;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.shipping.FindShippingPolicyBySellerIdsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculateReturnShippingFeeService 테스트")
class CalculateReturnShippingFeeUseCaseTest {

    @InjectMocks
    private CalculateReturnShippingFeeService service;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private FindPaymentAllocationPort findPaymentAllocationPort;

    @Mock
    private FindShippingPolicyBySellerIdsPort findShippingPolicyBySellerIdsPort;

    private static final Long ORDER_ID = 1L;
    private static final Long SELLER_ID = 100L;
    private static final Long PRICE_POLICY_ID_1 = 10L;
    private static final Long PRICE_POLICY_ID_2 = 20L;
    private static final long ONE_WAY_FEE = 3000L;
    private static final long FREE_SHIPPING_THRESHOLD = 30000L;

    @Nested
    @DisplayName("반품 택배비 계산 성공")
    class CalculateSuccess {

        @Test
        @DisplayName("case5: 판매자 귀책이면 반품 택배비 0원을 반환한다")
        void shouldReturnZeroWhenSellerFault() {
            // given
            Order order = createOrderWithProducts(SELLER_ID, PRICE_POLICY_ID_1, 20000L, 1);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(findPaymentAllocationPort.findByOrderId(ORDER_ID))
                    .thenReturn(List.of(createPaymentAllocation(SELLER_ID, 0L)));
            when(findShippingPolicyBySellerIdsPort.findBySellerIds(List.of(SELLER_ID)))
                    .thenReturn(Map.of(SELLER_ID, createShippingPolicy(SELLER_ID)));

            CalculateReturnShippingFeeCommand command = CalculateReturnShippingFeeCommand.builder()
                    .orderId(ORDER_ID)
                    .reasonCategory(OrderStatusReasonCategory.PRODUCT_DAMAGE)
                    .returnPricePolicyIds(null)
                    .build();

            // when
            CalculateReturnShippingFeeResult result = service.calculateReturnShippingFee(command);

            // then
            assertThat(result.returnShippingFee()).isZero();
        }

        @Test
        @DisplayName("case2: 고객 귀책 + 초기 유료 배송이면 편도 배송비를 반환한다")
        void shouldReturnOneWayFeeWhenBuyerFaultAndPaidShipping() {
            // given
            Order order = createOrderWithProducts(SELLER_ID, PRICE_POLICY_ID_1, 20000L, 1);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(findPaymentAllocationPort.findByOrderId(ORDER_ID))
                    .thenReturn(List.of(createPaymentAllocation(SELLER_ID, ONE_WAY_FEE)));
            when(findShippingPolicyBySellerIdsPort.findBySellerIds(List.of(SELLER_ID)))
                    .thenReturn(Map.of(SELLER_ID, createShippingPolicy(SELLER_ID)));

            CalculateReturnShippingFeeCommand command = CalculateReturnShippingFeeCommand.builder()
                    .orderId(ORDER_ID)
                    .reasonCategory(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND)
                    .returnPricePolicyIds(null)
                    .build();

            // when
            CalculateReturnShippingFeeResult result = service.calculateReturnShippingFee(command);

            // then
            assertThat(result.returnShippingFee()).isEqualTo(ONE_WAY_FEE);
        }

        @Test
        @DisplayName("case1: 고객 귀책 + 초기 무료 배송 + 전체 반품이면 왕복 배송비를 반환한다")
        void shouldReturnRoundTripFeeWhenBuyerFaultFreeShippingFullReturn() {
            // given
            Order order = createOrderWithProducts(SELLER_ID, PRICE_POLICY_ID_1, 50000L, 1);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(findPaymentAllocationPort.findByOrderId(ORDER_ID))
                    .thenReturn(List.of(createPaymentAllocation(SELLER_ID, 0L)));
            when(findShippingPolicyBySellerIdsPort.findBySellerIds(List.of(SELLER_ID)))
                    .thenReturn(Map.of(SELLER_ID, createShippingPolicy(SELLER_ID)));

            CalculateReturnShippingFeeCommand command = CalculateReturnShippingFeeCommand.builder()
                    .orderId(ORDER_ID)
                    .reasonCategory(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND)
                    .returnPricePolicyIds(null)
                    .build();

            // when
            CalculateReturnShippingFeeResult result = service.calculateReturnShippingFee(command);

            // then
            assertThat(result.returnShippingFee()).isEqualTo(ONE_WAY_FEE * 2);
        }

        @Test
        @DisplayName("case3: 고객 귀책 + 초기 무료 배송 + 부분 반품 + 무료 배송 기준 미달이면 왕복 배송비를 반환한다")
        void shouldReturnRoundTripFeeWhenPartialReturnBelowThreshold() {
            // given
            Order order = createOrderWithTwoProducts(SELLER_ID, PRICE_POLICY_ID_1, 20000L, 1, PRICE_POLICY_ID_2, 15000L, 1);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(findPaymentAllocationPort.findByOrderId(ORDER_ID))
                    .thenReturn(List.of(createPaymentAllocation(SELLER_ID, 0L)));
            when(findShippingPolicyBySellerIdsPort.findBySellerIds(List.of(SELLER_ID)))
                    .thenReturn(Map.of(SELLER_ID, createShippingPolicy(SELLER_ID)));

            CalculateReturnShippingFeeCommand command = CalculateReturnShippingFeeCommand.builder()
                    .orderId(ORDER_ID)
                    .reasonCategory(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND)
                    .returnPricePolicyIds(List.of(PRICE_POLICY_ID_1))
                    .build();

            // when
            CalculateReturnShippingFeeResult result = service.calculateReturnShippingFee(command);

            // then
            assertThat(result.returnShippingFee()).isEqualTo(ONE_WAY_FEE * 2);
        }

        @Test
        @DisplayName("case4: 고객 귀책 + 초기 무료 배송 + 부분 반품 + 무료 배송 기준 유지이면 편도 배송비를 반환한다")
        void shouldReturnOneWayFeeWhenPartialReturnMeetsThreshold() {
            // given
            Order order = createOrderWithTwoProducts(SELLER_ID, PRICE_POLICY_ID_1, 10000L, 1, PRICE_POLICY_ID_2, 35000L, 1);
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(findPaymentAllocationPort.findByOrderId(ORDER_ID))
                    .thenReturn(List.of(createPaymentAllocation(SELLER_ID, 0L)));
            when(findShippingPolicyBySellerIdsPort.findBySellerIds(List.of(SELLER_ID)))
                    .thenReturn(Map.of(SELLER_ID, createShippingPolicy(SELLER_ID)));

            CalculateReturnShippingFeeCommand command = CalculateReturnShippingFeeCommand.builder()
                    .orderId(ORDER_ID)
                    .reasonCategory(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND)
                    .returnPricePolicyIds(List.of(PRICE_POLICY_ID_1))
                    .build();

            // when
            CalculateReturnShippingFeeResult result = service.calculateReturnShippingFee(command);

            // then
            assertThat(result.returnShippingFee()).isEqualTo(ONE_WAY_FEE);
        }

        @Test
        @DisplayName("다중 판매자 주문에서 판매자별 반품 택배비를 합산한다")
        void shouldSumReturnShippingFeePerSeller() {
            // given
            Long sellerId2 = 200L;
            OrderProduct product1 = createOrderProduct(SELLER_ID, PRICE_POLICY_ID_1, 50000L, 1);
            OrderProduct product2 = createOrderProduct(sellerId2, PRICE_POLICY_ID_2, 40000L, 1);
            Order order = createOrderWithProductList(List.of(product1, product2));

            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);
            when(findPaymentAllocationPort.findByOrderId(ORDER_ID))
                    .thenReturn(List.of(
                            createPaymentAllocation(SELLER_ID, 0L),
                            createPaymentAllocation(sellerId2, 0L)
                    ));
            when(findShippingPolicyBySellerIdsPort.findBySellerIds(anyList()))
                    .thenReturn(Map.of(
                            SELLER_ID, createShippingPolicy(SELLER_ID),
                            sellerId2, new ShippingPolicyInfoResult(sellerId2, ONE_WAY_FEE, FREE_SHIPPING_THRESHOLD, 0L, 0L)
                    ));

            CalculateReturnShippingFeeCommand command = CalculateReturnShippingFeeCommand.builder()
                    .orderId(ORDER_ID)
                    .reasonCategory(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND)
                    .returnPricePolicyIds(null)
                    .build();

            // when
            CalculateReturnShippingFeeResult result = service.calculateReturnShippingFee(command);

            // then
            assertThat(result.returnShippingFee()).isEqualTo(ONE_WAY_FEE * 2 + ONE_WAY_FEE * 2);
        }
    }

    @Nested
    @DisplayName("반품 택배비 계산 실패")
    class CalculateFailure {

        @Test
        @DisplayName("reasonCategory가 null이면 ReasonCategoryNoValueException을 던진다")
        void shouldThrowWhenReasonCategoryIsNull() {
            // given
            CalculateReturnShippingFeeCommand command = CalculateReturnShippingFeeCommand.builder()
                    .orderId(ORDER_ID)
                    .reasonCategory(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> service.calculateReturnShippingFee(command))
                    .isInstanceOf(ReasonCategoryNoValueException.class);

            verifyNoInteractions(getOrderUseCase);
            verifyNoInteractions(findPaymentAllocationPort);
            verifyNoInteractions(findShippingPolicyBySellerIdsPort);
        }

        @Test
        @DisplayName("취소 전용 사유(CANCEL_ORDER)이면 InvalidReasonCategoryException을 던진다")
        void shouldThrowWhenCancelOnlyReasonCategory() {
            // given
            CalculateReturnShippingFeeCommand command = CalculateReturnShippingFeeCommand.builder()
                    .orderId(ORDER_ID)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .build();

            // when & then
            assertThatThrownBy(() -> service.calculateReturnShippingFee(command))
                    .isInstanceOf(InvalidReasonCategoryException.class);

            verifyNoInteractions(getOrderUseCase);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrderWithProducts(Long sellerId, Long pricePolicyId, Long unitAmount, int quantity) {
        OrderProduct product = createOrderProduct(sellerId, pricePolicyId, unitAmount, quantity);
        return createOrderWithProductList(List.of(product));
    }

    private Order createOrderWithTwoProducts(
            Long sellerId,
            Long pricePolicyId1, Long unitAmount1, int quantity1,
            Long pricePolicyId2, Long unitAmount2, int quantity2
    ) {
        OrderProduct product1 = createOrderProduct(sellerId, pricePolicyId1, unitAmount1, quantity1);
        OrderProduct product2 = createOrderProduct(sellerId, pricePolicyId2, unitAmount2, quantity2);
        return createOrderWithProductList(List.of(product1, product2));
    }

    private OrderProduct createOrderProduct(Long sellerId, Long pricePolicyId, Long unitAmount, int quantity) {
        return OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(ORDER_ID)
                .sellerId(sellerId)
                .pricePolicyId(pricePolicyId)
                .unitAmount(unitAmount)
                .quantity(quantity)
                .orderStatus(OrderStatus.DELIVERED)
                .build());
    }

    private Order createOrderWithProductList(List<OrderProduct> products) {
        return Order.from(OrderSnapshotState.builder()
                .id(ORDER_ID)
                .buyerId(1L)
                .orderStatus(OrderStatus.DELIVERED)
                .orderProductStates(products.stream()
                        .map(this::toSnapshotState)
                        .toList())
                .build());
    }

    private OrderProductSnapshotState toSnapshotState(OrderProduct product) {
        return OrderProductSnapshotState.builder()
                .orderId(product.getOrderId())
                .sellerId(product.getSellerId())
                .pricePolicyId(product.getPricePolicyId())
                .unitAmount(product.getUnitAmount())
                .quantity(product.getQuantity())
                .orderStatus(product.getOrderStatus())
                .build();
    }

    private PaymentAllocation createPaymentAllocation(Long sellerId, Long shippingFee) {
        return PaymentAllocation.from(PaymentAllocationSnapshotState.builder()
                .id(1L)
                .orderId(ORDER_ID)
                .sellerId(sellerId)
                .allocatedAmount(50000L)
                .shippingFee(shippingFee)
                .build());
    }

    private ShippingPolicyInfoResult createShippingPolicy(Long sellerId) {
        return new ShippingPolicyInfoResult(sellerId, ONE_WAY_FEE, FREE_SHIPPING_THRESHOLD, 0L, 0L);
    }
}
