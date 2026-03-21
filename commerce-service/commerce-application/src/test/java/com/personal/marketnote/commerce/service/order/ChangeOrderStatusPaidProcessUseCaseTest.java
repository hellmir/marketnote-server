package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.DeleteOrderedCartProductsPort;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeOrderStatusPaidProcessUseCaseTest {
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
    @Mock
    private PublishOrderEventPort publishOrderEventPort;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private ChangeOrderStatusService changeOrderStatusService;

    @Nested
    @DisplayName("PAID 상태 변경 시 공유 구매 적립 예정 포인트 추가")
    class SharedPurchasePendingPointTest {

        @Test
        @DisplayName("공유자가 있는 주문이 PAID로 변경되면 addPendingSharedPurchasePoints가 호출된다")
        void shouldCallAddPendingSharedPurchasePointsWhenSharerExists() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long sharerId = 200L;
            Long totalAmount = 50000L;
            Long pricePolicyId = 10L;

            Order order = createOrderWithSharer(orderId, buyerId, sharerId, pricePolicyId, totalAmount);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, createProductInfoResult()));

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort).addPendingSharedPurchasePoints(
                    List.of(sharerId), totalAmount, orderId
            );
        }

        @Test
        @DisplayName("공유자가 없는 주문이 PAID로 변경되면 addPendingSharedPurchasePoints가 호출되지 않는다")
        void shouldNotCallAddPendingSharedPurchasePointsWhenNoSharer() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId = 10L;

            Order order = createOrderWithoutSharer(orderId, buyerId, pricePolicyId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, createProductInfoResult()));

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort, never()).addPendingSharedPurchasePoints(
                    anyList(), anyLong(), anyLong()
            );
        }

        @Test
        @DisplayName("addPendingSharedPurchasePoints 호출 실패 시 예외가 전파되지 않는다")
        void shouldNotPropagateExceptionWhenAddPendingSharedPurchasePointsFails() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long sharerId = 200L;
            Long totalAmount = 50000L;
            Long pricePolicyId = 10L;

            Order order = createOrderWithSharer(orderId, buyerId, sharerId, pricePolicyId, totalAmount);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, createProductInfoResult()));
            doThrow(new RuntimeException("리워드 서비스 요청 실패"))
                    .when(modifyUserPointPort).addPendingSharedPurchasePoints(anyList(), anyLong(), anyLong());

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }
    }

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

            Order order = createOrderWithoutSharer(orderId, buyerId, pricePolicyId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, createProductInfoResultWithPoint(accumulatedPoint)));

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(buyerId, accumulatedPoint, orderId);
        }

        @Test
        @DisplayName("여러 상품의 적립 포인트가 수량에 비례하여 합산된다")
        void shouldSumAccumulatedPointsByQuantity() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId1 = 10L;
            Long pricePolicyId2 = 20L;

            Order order = createOrderWithMultipleProducts(orderId, buyerId, pricePolicyId1, 2, pricePolicyId2, 3);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId1, pricePolicyId2)))
                    .thenReturn(Map.of(
                            pricePolicyId1, createProductInfoResultWithPoint(500L),
                            pricePolicyId2, createProductInfoResultWithPoint(300L)
                    ));

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            Long expectedTotal = 500L * 2 + 300L * 3;
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(buyerId, expectedTotal, orderId);
        }

        @Test
        @DisplayName("상품 정보 조회 결과에 해당 상품이 없으면 해당 상품의 적립 포인트를 건너뛴다")
        void shouldSkipWhenProductInfoNotFound() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId1 = 10L;
            Long pricePolicyId2 = 20L;

            Order order = createOrderWithMultipleProducts(orderId, buyerId, pricePolicyId1, 1, pricePolicyId2, 1);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId1, pricePolicyId2)))
                    .thenReturn(Map.of(pricePolicyId1, createProductInfoResultWithPoint(500L)));

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(buyerId, 500L, orderId);
        }

        @Test
        @DisplayName("상품의 적립 포인트가 null이면 해당 상품의 적립 포인트를 건너뛴다")
        void shouldSkipWhenAccumulatedPointIsNull() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId1 = 10L;
            Long pricePolicyId2 = 20L;

            Order order = createOrderWithMultipleProducts(orderId, buyerId, pricePolicyId1, 1, pricePolicyId2, 1);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId1, pricePolicyId2)))
                    .thenReturn(Map.of(
                            pricePolicyId1, createProductInfoResultWithPoint(500L),
                            pricePolicyId2, createProductInfoResultWithPoint(null)
                    ));

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(buyerId, 500L, orderId);
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
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, createProductInfoResultWithPoint(0L)));

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(modifyUserPointPort, never()).addPendingProductAccumulationPoints(anyLong(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("addPendingProductAccumulationPoints 호출 실패 시 예외가 전파되지 않는다")
        void shouldNotPropagateExceptionWhenAddPendingProductAccumulationPointsFails() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId = 10L;

            Order order = createOrderWithoutSharer(orderId, buyerId, pricePolicyId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, createProductInfoResultWithPoint(500L)));
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

    private Order createOrderWithSharer(Long orderId, Long buyerId, Long sharerId, Long pricePolicyId, Long totalAmount) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .sharerId(sharerId)
                        .quantity(1)
                        .unitAmount(totalAmount)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .totalAmount(totalAmount)
                .couponAmount(0L)
                .pointAmount(0L)
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithoutSharer(Long orderId, Long buyerId, Long pricePolicyId) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .totalAmount(50000L)
                .couponAmount(0L)
                .pointAmount(0L)
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
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId1)
                        .quantity(quantity1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId2)
                        .quantity(quantity2)
                        .unitAmount(30000L)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .totalAmount(130000L)
                .couponAmount(0L)
                .pointAmount(0L)
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
