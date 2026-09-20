package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeOrderStatusPaidProcessUseCaseTest {
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

    @Nested
    @DisplayName("PAID 상태 변경 시 상품 구매 적립 예정 포인트 추가")
    class ProductAccumulationPendingPointTest {

        @Test
        @DisplayName("상품 적립 포인트가 있는 주문이 PAID로 변경되면 addPendingProductAccumulationPoints가 호출된다")
        void shouldCallAddPendingProductAccumulationPointsWhenAccumulatedPointExists() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId = 10L;
            Long accumulatedPoint = 500L;

            Order order = createOrderWithoutSharer(orderId, buyerId, pricePolicyId, accumulatedPoint);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(buyerId, accumulatedPoint, orderId);
            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("여러 상품의 적립 포인트가 수량에 비례하여 합산된다")
        void shouldSumAccumulatedPointsByQuantity() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId1 = 10L;
            Long pricePolicyId2 = 20L;

            Order order = createOrderWithMultipleProducts(orderId, buyerId, pricePolicyId1, 2, 500L, pricePolicyId2, 3, 300L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            Long expectedTotal = 500L * 2 + 300L * 3;
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(buyerId, expectedTotal, orderId);
            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("일부 상품의 적립 포인트가 0이면 0은 합산에 포함되지만 결과에 영향을 주지 않는다")
        void shouldIncludeZeroPointProductsInSum() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId1 = 10L;
            Long pricePolicyId2 = 20L;

            Order order = createOrderWithMultipleProducts(orderId, buyerId, pricePolicyId1, 1, 500L, pricePolicyId2, 1, 0L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(buyerId, 500L, orderId);
            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("한 상품의 적립 포인트가 0이면 해당 상품은 합산에 0으로 포함된다")
        void shouldSumWithZeroWhenAccumulatedPointIsZero() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId1 = 10L;
            Long pricePolicyId2 = 20L;

            Order order = createOrderWithMultipleProducts(orderId, buyerId, pricePolicyId1, 1, 500L, pricePolicyId2, 1, 0L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(buyerId, 500L, orderId);
            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("모든 상품의 적립 포인트가 0이면 addPendingProductAccumulationPoints가 호출되지 않는다")
        void shouldNotCallWhenTotalAccumulatedPointIsZero() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId = 10L;

            Order order = createOrderWithoutSharer(orderId, buyerId, pricePolicyId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort, never()).addPendingProductAccumulationPoints(anyLong(), anyLong(), anyLong());
            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("addPendingProductAccumulationPoints 호출 실패 시 예외가 전파되지 않는다")
        void shouldNotPropagateExceptionWhenAddPendingProductAccumulationPointsFails() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId = 10L;

            Order order = createOrderWithoutSharer(orderId, buyerId, pricePolicyId, 500L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            doThrow(new RuntimeException("리워드 서비스 요청 실패"))
                    .when(modifyUserPointPort).addPendingProductAccumulationPoints(anyLong(), anyLong(), anyLong());

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private ProductInfoResult createProductInfoResult() {
        return new ProductInfoResult(1L, 1L, "테스트 상품", "브랜드", 50000L, null, 500L, List.of());
    }

    private ProductInfoResult createProductInfoResultWithPoint(Long accumulatedPoint) {
        return new ProductInfoResult(1L, 1L, "테스트 상품", "브랜드", 50000L, null, accumulatedPoint, List.of());
    }

    private Order createOrderWithSharer(Long orderId, Long buyerId, UUID sharerKey, Long pricePolicyId, Long totalAmount) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .sharerKey(sharerKey)
                        .quantity(1)
                        .unitAmount(totalAmount)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .accumulatedPoint(0L)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-" + orderId))
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .amount(OrderAmount.of(totalAmount, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithoutSharer(Long orderId, Long buyerId, Long pricePolicyId) {
        return createOrderWithoutSharer(orderId, buyerId, pricePolicyId, 0L);
    }

    private Order createOrderWithoutSharer(Long orderId, Long buyerId, Long pricePolicyId, Long accumulatedPoint) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .accumulatedPoint(accumulatedPoint)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-" + orderId))
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .amount(OrderAmount.of(50000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithMultipleProducts(
            Long orderId, Long buyerId,
            Long pricePolicyId1, int quantity1,
            Long pricePolicyId2, int quantity2
    ) {
        return createOrderWithMultipleProducts(orderId, buyerId, pricePolicyId1, quantity1, 0L, pricePolicyId2, quantity2, 0L);
    }

    private Order createOrderWithMultipleProducts(
            Long orderId, Long buyerId,
            Long pricePolicyId1, int quantity1, Long accPoint1,
            Long pricePolicyId2, int quantity2, Long accPoint2
    ) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId1)
                        .quantity(quantity1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .accumulatedPoint(accPoint1)
                        .build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId2)
                        .quantity(quantity2)
                        .unitAmount(30000L)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .accumulatedPoint(accPoint2)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-" + orderId))
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .amount(OrderAmount.of(130000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
