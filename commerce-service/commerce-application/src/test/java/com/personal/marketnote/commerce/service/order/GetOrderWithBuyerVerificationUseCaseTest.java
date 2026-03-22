package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderKeyResult;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderResult;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderProductPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderWithBuyerVerificationUseCaseTest {
    @Mock
    private FindOrderPort findOrderPort;
    @Mock
    private FindOrderProductPort findOrderProductPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private GetOrderService getOrderService;

    // ==================================================================================
    // getOrderAndOrderProducts(id, buyerId) - 소유자 검증 포함
    // ==================================================================================

    @Nested
    @DisplayName("getOrderAndOrderProducts(id, buyerId) - 소유자 검증 성공")
    class GetOrderAndOrderProductsWithBuyerIdSuccessTest {

        @Test
        @DisplayName("주문 소유자와 요청 buyerId가 일치하면 주문 정보를 반환한다")
        void ownerMatches_returnsOrderResult() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId, buyerId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(orderId);
            assertThat(result.buyerId()).isEqualTo(buyerId);
        }

        @Test
        @DisplayName("소유자 검증 통과 후 상품 정보를 포함한 결과를 반환한다")
        void ownerMatches_returnsOrderWithProducts() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId = 200L;
            Order order = createOrderWithBuyerIdAndProducts(orderId, buyerId, List.of(pricePolicyId));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            ProductInfoResult productInfo = new ProductInfoResult(1L, null, "테스트 상품", "테스트 브랜드", null, null, null, List.of());
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, productInfo));

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId, buyerId);

            assertThat(result.orderProducts()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getOrderAndOrderProducts(id, buyerId) - 소유자 검증 실패")
    class GetOrderAndOrderProductsWithBuyerIdFailureTest {

        @Test
        @DisplayName("주문 소유자와 요청 buyerId가 다르면 UnauthorizedOrderAccessException이 발생한다")
        void ownerMismatch_throwsUnauthorizedOrderAccessException() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long requestedBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> getOrderService.getOrderAndOrderProducts(orderId, requestedBuyerId))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }

        @Test
        @DisplayName("소유자 검증 실패 시 예외 메시지에 내부 ID가 노출되지 않는다")
        void ownerMismatch_exceptionDoesNotContainInternalId() {
            Long orderId = 123L;
            Long ownerBuyerId = 100L;
            Long requestedBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> getOrderService.getOrderAndOrderProducts(orderId, requestedBuyerId))
                    .isInstanceOf(UnauthorizedOrderAccessException.class)
                    .hasMessageContaining("ERR_ORDER_AUTH_01")
                    .hasMessageNotContaining("123")
                    .hasMessageNotContaining("999");
        }

        @Test
        @DisplayName("소유자 검증 실패 시 상품 정보 조회를 수행하지 않는다")
        void ownerMismatch_doesNotCallProductPort() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long requestedBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> getOrderService.getOrderAndOrderProducts(orderId, requestedBuyerId))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("주문이 존재하지 않으면 OrderNotFoundException이 발생한다")
        void orderNotFound_throwsOrderNotFoundException() {
            Long orderId = 999L;
            Long buyerId = 100L;

            when(findOrderPort.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getOrderService.getOrderAndOrderProducts(orderId, buyerId))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    // ==================================================================================
    // getOrderKey(id, buyerId) - 소유자 검증 포함
    // ==================================================================================

    @Nested
    @DisplayName("getOrderKey(id, buyerId) - 소유자 검증 성공")
    class GetOrderKeyWithBuyerIdSuccessTest {

        @Test
        @DisplayName("주문 소유자와 요청 buyerId가 일치하면 주문 키를 반환한다")
        void ownerMatches_returnsOrderKey() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            GetOrderKeyResult result = getOrderService.getOrderKey(orderId, buyerId);

            assertThat(result).isNotNull();
            assertThat(result.orderKey()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getOrderKey(id, buyerId) - 소유자 검증 실패")
    class GetOrderKeyWithBuyerIdFailureTest {

        @Test
        @DisplayName("주문 소유자와 요청 buyerId가 다르면 UnauthorizedOrderAccessException이 발생한다")
        void ownerMismatch_throwsUnauthorizedOrderAccessException() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long requestedBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> getOrderService.getOrderKey(orderId, requestedBuyerId))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }

        @Test
        @DisplayName("주문이 존재하지 않으면 OrderNotFoundException이 발생한다")
        void orderNotFound_throwsOrderNotFoundException() {
            Long orderId = 999L;
            Long buyerId = 100L;

            when(findOrderPort.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getOrderService.getOrderKey(orderId, buyerId))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    // ==================================================================================
    // 기존 getOrder(id) 검증 없음 확인
    // ==================================================================================

    @Nested
    @DisplayName("getOrder(id) - 기존 내부용 메서드는 소유자 검증 없음")
    class GetOrderInternalTest {

        @Test
        @DisplayName("getOrder(id)는 buyerId 검증 없이 주문을 반환한다")
        void getOrder_noBuyerVerification_returnsOrder() {
            Long orderId = 1L;
            Order order = createOrderWithBuyerId(orderId, 100L);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orderId);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrderWithBuyerId(Long orderId, Long buyerId) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .totalAmount(100000L)
                .couponAmount(0L)
                .pointAmount(0L)
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null))
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithBuyerIdAndProducts(Long orderId, Long buyerId, List<Long> pricePolicyIds) {
        List<OrderProductSnapshotState> productStates = pricePolicyIds.stream()
                .map(pricePolicyId -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAID)
                        .build())
                .toList();

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .totalAmount(100000L)
                .couponAmount(0L)
                .pointAmount(0L)
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
