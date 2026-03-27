package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderProductResult;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderResult;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderProductPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.product.result.ProductOptionInfoResult;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetOrderAndOrderProductsUseCaseTest {
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
    // 주문 조회 성공 케이스
    // ==================================================================================

    @Nested
    @DisplayName("주문 및 주문 상품 조회 성공 케이스")
    class GetOrderAndOrderProductsSuccessTest {

        @Test
        @DisplayName("주문 ID로 주문과 주문 상품 정보를 조회한다")
        void getOrderAndOrderProducts_success_returnsOrderWithProducts() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 2, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            ProductInfoResult productInfo = new ProductInfoResult(
                    1L, null, "테스트 상품", "테스트 브랜드", null, null, null, List.of()
            );
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, productInfo));

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(orderId);
            assertThat(result.orderProducts()).hasSize(1);
        }

        @Test
        @DisplayName("복수 상품이 포함된 주문을 조회한다")
        void getOrderAndOrderProducts_multipleProducts_returnsAllProducts() {
            Long orderId = 1L;
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            Long pricePolicyId3 = 300L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId1, 10L, 1, 30000L),
                    createOrderProduct(pricePolicyId2, 20L, 2, 50000L),
                    createOrderProduct(pricePolicyId3, 30L, 3, 70000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Map<Long, ProductInfoResult> productInfoMap = Map.of(
                    pricePolicyId1, new ProductInfoResult(1L, null, "상품1", "브랜드1", null, null, null, List.of()),
                    pricePolicyId2, new ProductInfoResult(2L, null, "상품2", "브랜드2", null, null, null, List.of()),
                    pricePolicyId3, new ProductInfoResult(3L, null, "상품3", "브랜드3", null, null, null, List.of())
            );
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(productInfoMap);

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts()).hasSize(3);
        }

        @Test
        @DisplayName("상품 정보가 없는 주문 상품도 정상 조회된다")
        void getOrderAndOrderProducts_noProductInfo_returnsOrderWithNullProductInfo() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result).isNotNull();
            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).productName()).isNull();
            assertThat(result.orderProducts().get(0).brandName()).isNull();
            assertThat(result.orderProducts().get(0).productId()).isNull();
        }

        @Test
        @DisplayName("일부 상품 정보만 존재하는 경우 존재하는 정보만 매핑된다")
        void getOrderAndOrderProducts_partialProductInfo_mapsExistingInfoOnly() {
            Long orderId = 1L;
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId1, 10L, 1, 30000L),
                    createOrderProduct(pricePolicyId2, 20L, 2, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Map<Long, ProductInfoResult> productInfoMap = new HashMap<>();
            productInfoMap.put(pricePolicyId1, new ProductInfoResult(1L, null, "상품1", "브랜드1", null, null, null, List.of()));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(productInfoMap);

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts()).hasSize(2);

            GetOrderProductResult product1 = result.orderProducts().stream()
                    .filter(p -> p.pricePolicyId().equals(pricePolicyId1))
                    .findFirst()
                    .orElseThrow();
            assertThat(product1.productName()).isEqualTo("상품1");
            assertThat(product1.brandName()).isEqualTo("브랜드1");

            GetOrderProductResult product2 = result.orderProducts().stream()
                    .filter(p -> p.pricePolicyId().equals(pricePolicyId2))
                    .findFirst()
                    .orElseThrow();
            assertThat(product2.productName()).isNull();
            assertThat(product2.brandName()).isNull();
        }

        @Test
        @DisplayName("상품 옵션 정보가 포함된 주문 상품을 조회한다")
        void getOrderAndOrderProducts_withProductOptions_returnsOptionsInfo() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            List<ProductOptionInfoResult> options = List.of(
                    new ProductOptionInfoResult(1L, "색상: 블랙", "ACTIVE"),
                    new ProductOptionInfoResult(2L, "사이즈: L", "ACTIVE")
            );
            ProductInfoResult productInfo = new ProductInfoResult(
                    1L, null, "테스트 상품", "테스트 브랜드", null, null, null, options
            );
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, productInfo));

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).selectedOptions()).hasSize(2);
            assertThat(result.orderProducts().get(0).selectedOptions())
                    .extracting(ProductOptionInfoResult::content)
                    .containsExactly("색상: 블랙", "사이즈: L");
        }

        @Test
        @DisplayName("FindProductByPricePolicyPort가 null을 반환해도 정상 처리된다")
        void getOrderAndOrderProducts_nullProductInfoMap_returnsOrderWithEmptyProductInfo() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(null);

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result).isNotNull();
            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).productName()).isNull();
        }
    }

    // ==================================================================================
    // Order 속성 매핑 검증
    // ==================================================================================

    @Nested
    @DisplayName("Order 속성 매핑 검증")
    class OrderMappingTest {

        @Test
        @DisplayName("주문 ID가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsOrderIdCorrectly() {
            Long orderId = 999L;
            Order order = createOrder(orderId, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.id()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("buyerId가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsBuyerIdCorrectly() {
            Long orderId = 1L;
            Long buyerId = 123L;
            Order order = createOrderWithBuyerId(orderId, buyerId, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.buyerId()).isEqualTo(buyerId);
        }

        @Test
        @DisplayName("orderNumber가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsOrderNumberCorrectly() {
            Long orderId = 1L;
            String orderNumber = "ORD-2025-001";
            Order order = createOrderWithOrderNumber(orderId, orderNumber, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderNumber()).isEqualTo(orderNumber);
        }

        @Test
        @DisplayName("orderStatus가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsOrderStatusCorrectly() {
            Long orderId = 1L;
            OrderStatus orderStatus = OrderStatus.PAID;
            Order order = createOrderWithStatus(orderId, orderStatus, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderStatus()).isEqualTo(orderStatus);
        }

        @Test
        @DisplayName("statusChangeReasonCategory가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsStatusChangeReasonCategoryCorrectly() {
            Long orderId = 1L;
            OrderStatusReasonCategory reasonCategory = OrderStatusReasonCategory.CANCEL_ORDER;
            Order order = createOrderWithStatusChangeReason(orderId, reasonCategory, "취소 사유", List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.statusChangeReasonCategory()).isEqualTo(reasonCategory);
        }

        @Test
        @DisplayName("statusChangeReason이 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsStatusChangeReasonCorrectly() {
            Long orderId = 1L;
            String reason = "단순 변심으로 인한 취소";
            Order order = createOrderWithStatusChangeReason(orderId, OrderStatusReasonCategory.CANCEL_ORDER, reason, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.statusChangeReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("totalAmount가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsTotalAmountCorrectly() {
            Long orderId = 1L;
            Long totalAmount = 150000L;
            Order order = createOrderWithAmounts(orderId, totalAmount, null, 10000L, 5000L, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.totalAmount()).isEqualTo(totalAmount);
        }

        @Test
        @DisplayName("paidAmount가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsPaidAmountCorrectly() {
            Long orderId = 1L;
            Long paidAmount = 135000L;
            Order order = createOrderWithAmounts(orderId, 150000L, paidAmount, 10000L, 5000L, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.paidAmount()).isEqualTo(paidAmount);
        }

        @Test
        @DisplayName("couponAmount가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsCouponAmountCorrectly() {
            Long orderId = 1L;
            Long couponAmount = 10000L;
            Order order = createOrderWithAmounts(orderId, 150000L, null, couponAmount, 5000L, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.couponAmount()).isEqualTo(couponAmount);
        }

        @Test
        @DisplayName("pointAmount가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsPointAmountCorrectly() {
            Long orderId = 1L;
            Long pointAmount = 5000L;
            Order order = createOrderWithAmounts(orderId, 150000L, null, 10000L, pointAmount, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.pointAmount()).isEqualTo(pointAmount);
        }

        @Test
        @DisplayName("모든 금액이 null인 경우도 정상 처리된다")
        void getOrderAndOrderProducts_nullAmounts_returnsNullAmounts() {
            Long orderId = 1L;
            Order order = createOrderWithAmounts(orderId, null, null, null, null, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.totalAmount()).isNull();
            assertThat(result.paidAmount()).isNull();
            assertThat(result.couponAmount()).isNull();
            assertThat(result.pointAmount()).isNull();
        }
    }

    // ==================================================================================
    // OrderProduct 속성 매핑 검증
    // ==================================================================================

    @Nested
    @DisplayName("OrderProduct 속성 매핑 검증")
    class OrderProductMappingTest {

        @Test
        @DisplayName("주문 상품의 sellerId가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsSellerIdCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Long sellerId = 55L;
            Order order = createOrder(orderId, List.of(
                    createOrderProductWithSellerId(pricePolicyId, sellerId, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).sellerId()).isEqualTo(sellerId);
        }

        @Test
        @DisplayName("주문 상품의 pricePolicyId가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsPricePolicyIdCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 777L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(pricePolicyId);
        }

        @Test
        @DisplayName("주문 상품의 sharerId가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsSharerIdCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Long sharerId = 33L;
            Order order = createOrder(orderId, List.of(
                    createOrderProductWithSharerId(pricePolicyId, 10L, sharerId, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).sharerId()).isEqualTo(sharerId);
        }

        @Test
        @DisplayName("주문 상품의 sharerId가 null인 경우 null로 매핑된다")
        void getOrderAndOrderProducts_nullSharerId_mapsToNull() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProductWithSharerId(pricePolicyId, 10L, null, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).sharerId()).isNull();
        }

        @Test
        @DisplayName("주문 상품의 quantity가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsQuantityCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Integer quantity = 5;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, quantity, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).quantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("주문 상품의 unitAmount가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsUnitAmountCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Long unitAmount = 75000L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, unitAmount)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).unitAmount()).isEqualTo(unitAmount);
        }

        @Test
        @DisplayName("주문 상품의 imageUrl이 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsImageUrlCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            String imageUrl = "https://marketnote.example.com/images/product-123.png";
            Order order = createOrder(orderId, List.of(
                    createOrderProductWithImageUrl(pricePolicyId, 10L, 1, 50000L, imageUrl)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).imageUrl()).isEqualTo(imageUrl);
        }

        @Test
        @DisplayName("주문 상품의 orderStatus가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsOrderProductStatusCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            OrderStatus productStatus = OrderStatus.SHIPPING;
            Order order = createOrderWithProductStatus(orderId, pricePolicyId, productStatus);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).orderStatus()).isEqualTo(productStatus);
        }

        @Test
        @DisplayName("주문 상품의 isReviewed가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsIsReviewedCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProductWithReviewStatus(pricePolicyId, 10L, 1, 50000L, true)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).isReviewed()).isTrue();
        }

        @Test
        @DisplayName("주문 상품의 isReviewed가 false인 경우 false로 매핑된다")
        void getOrderAndOrderProducts_isReviewedFalse_mapsFalse() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProductWithReviewStatus(pricePolicyId, 10L, 1, 50000L, false)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).isReviewed()).isFalse();
        }
    }

    // ==================================================================================
    // ProductInfo 매핑 검증
    // ==================================================================================

    @Nested
    @DisplayName("ProductInfo 매핑 검증")
    class ProductInfoMappingTest {

        @Test
        @DisplayName("상품 ID가 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsProductIdCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Long productId = 888L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            ProductInfoResult productInfo = new ProductInfoResult(
                    productId, null, "테스트 상품", "테스트 브랜드", null, null, null, List.of()
            );
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, productInfo));

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).productId()).isEqualTo(productId);
        }

        @Test
        @DisplayName("상품명이 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsProductNameCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            String productName = "프리미엄 티셔츠";
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            ProductInfoResult productInfo = new ProductInfoResult(
                    1L, null, productName, "테스트 브랜드", null, null, null, List.of()
            );
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, productInfo));

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).productName()).isEqualTo(productName);
        }

        @Test
        @DisplayName("브랜드명이 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsBrandNameCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            String brandName = "나이키";
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            ProductInfoResult productInfo = new ProductInfoResult(
                    1L, null, "테스트 상품", brandName, null, null, null, List.of()
            );
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, productInfo));

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).brandName()).isEqualTo(brandName);
        }

        @Test
        @DisplayName("상품 옵션이 정확하게 매핑된다")
        void getOrderAndOrderProducts_mapsSelectedOptionsCorrectly() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            List<ProductOptionInfoResult> options = List.of(
                    new ProductOptionInfoResult(1L, "색상: 레드", "ACTIVE"),
                    new ProductOptionInfoResult(2L, "사이즈: M", "ACTIVE"),
                    new ProductOptionInfoResult(3L, "소재: 면", "ACTIVE")
            );
            ProductInfoResult productInfo = new ProductInfoResult(
                    1L, null, "테스트 상품", "테스트 브랜드", null, null, null, options
            );
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                    .thenReturn(Map.of(pricePolicyId, productInfo));

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).selectedOptions()).hasSize(3);
            assertThat(result.orderProducts().get(0).selectedOptions())
                    .extracting(ProductOptionInfoResult::content)
                    .containsExactly("색상: 레드", "사이즈: M", "소재: 면");
        }

        @Test
        @DisplayName("상품 정보가 없으면 selectedOptions가 빈 리스트로 매핑된다")
        void getOrderAndOrderProducts_noProductInfo_returnsEmptyOptions() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts().get(0).selectedOptions()).isEmpty();
        }
    }

    // ==================================================================================
    // 실패 케이스
    // ==================================================================================

    @Nested
    @DisplayName("주문 조회 실패 케이스")
    class GetOrderAndOrderProductsFailureTest {

        @Test
        @DisplayName("주문이 존재하지 않으면 OrderNotFoundException이 발생한다")
        void getOrderAndOrderProducts_orderNotFound_throwsException() {
            Long orderId = 999L;

            when(findOrderPort.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getOrderService.getOrderAndOrderProducts(orderId))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessageContaining("주문을 찾을 수 없습니다")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("주문이 존재하지 않으면 상품 정보 조회를 수행하지 않는다")
        void getOrderAndOrderProducts_orderNotFound_doesNotCallProductPort() {
            Long orderId = 999L;

            when(findOrderPort.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getOrderService.getOrderAndOrderProducts(orderId))
                    .isInstanceOf(OrderNotFoundException.class);

            verifyNoInteractions(findProductByPricePolicyPort);
        }
    }

    // ==================================================================================
    // 예외 전파 케이스
    // ==================================================================================

    @Nested
    @DisplayName("예외 전파 케이스")
    class ExceptionPropagationTest {

        @Test
        @DisplayName("FindOrderPort.findById 예외 발생 시 예외를 전파한다")
        void getOrderAndOrderProducts_findOrderPortFails_propagates() {
            Long orderId = 1L;
            RuntimeException exception = new RuntimeException("DB 연결 실패");

            when(findOrderPort.findById(orderId)).thenThrow(exception);

            assertThatThrownBy(() -> getOrderService.getOrderAndOrderProducts(orderId))
                    .isSameAs(exception);

            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("FindProductByPricePolicyPort.findByPricePolicyIds 예외 발생 시 예외를 전파한다")
        void getOrderAndOrderProducts_findProductPortFails_propagates() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));
            RuntimeException exception = new RuntimeException("상품 서비스 연결 실패");

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenThrow(exception);

            assertThatThrownBy(() -> getOrderService.getOrderAndOrderProducts(orderId))
                    .isSameAs(exception);
        }
    }

    // ==================================================================================
    // 호출 순서 검증
    // ==================================================================================

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrderTest {

        @Test
        @DisplayName("주문 조회 -> 상품 정보 조회 순서로 호출한다")
        void getOrderAndOrderProducts_callsInOrder() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            getOrderService.getOrderAndOrderProducts(orderId);

            InOrder inOrder = inOrder(findOrderPort, findProductByPricePolicyPort);
            inOrder.verify(findOrderPort).findById(orderId);
            inOrder.verify(findProductByPricePolicyPort).findByPricePolicyIds(List.of(pricePolicyId));
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("주문 조회 시 정확한 ID로 FindOrderPort를 호출한다")
        void getOrderAndOrderProducts_callsFindOrderPortWithCorrectId() {
            Long orderId = 12345L;
            Order order = createOrder(orderId, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            getOrderService.getOrderAndOrderProducts(orderId);

            verify(findOrderPort).findById(12345L);
        }

        @Test
        @DisplayName("상품 정보 조회 시 주문 상품의 pricePolicyId 목록으로 호출한다")
        void getOrderAndOrderProducts_callsFindProductPortWithPricePolicyIds() {
            Long orderId = 1L;
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId1, 10L, 1, 30000L),
                    createOrderProduct(pricePolicyId2, 20L, 2, 50000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            getOrderService.getOrderAndOrderProducts(orderId);

            verify(findProductByPricePolicyPort).findByPricePolicyIds(List.of(pricePolicyId1, pricePolicyId2));
        }
    }

    // ==================================================================================
    // 엣지 케이스
    // ==================================================================================

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCaseTest {

        @Test
        @DisplayName("주문 상품이 빈 목록인 경우 빈 상품 목록을 반환한다")
        void getOrderAndOrderProducts_emptyOrderProducts_returnsEmptyList() {
            Long orderId = 1L;
            Order order = createOrder(orderId, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("주문 상품이 빈 목록인 경우 빈 pricePolicyIds로 상품 조회를 호출한다")
        void getOrderAndOrderProducts_emptyOrderProducts_callsWithEmptyList() {
            Long orderId = 1L;
            Order order = createOrder(orderId, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of()))
                    .thenReturn(Map.of());

            getOrderService.getOrderAndOrderProducts(orderId);

            verify(findProductByPricePolicyPort).findByPricePolicyIds(List.of());
        }

        @Test
        @DisplayName("다양한 상태의 주문 상품이 있는 경우 각각의 상태가 올바르게 매핑된다")
        void getOrderAndOrderProducts_variousOrderStatuses_mapsCorrectly() {
            Long orderId = 1L;
            Order order = createOrderWithVariousProductStatuses(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts()).hasSize(3);
        }

        @Test
        @DisplayName("주문 ID가 1인 경우 정상 처리된다")
        void getOrderAndOrderProducts_minOrderId_succeeds() {
            Long orderId = 1L;
            Order order = createOrder(orderId, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("주문 ID가 큰 값인 경우 정상 처리된다")
        void getOrderAndOrderProducts_largeOrderId_succeeds() {
            Long orderId = Long.MAX_VALUE - 1;
            Order order = createOrder(orderId, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.id()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("동일한 pricePolicyId를 가진 복수 상품이 있는 경우 모두 조회된다")
        void getOrderAndOrderProducts_duplicatePricePolicyIds_returnsAll() {
            Long orderId = 1L;
            Long pricePolicyId = 100L;
            Order order = createOrder(orderId, List.of(
                    createOrderProduct(pricePolicyId, 10L, 1, 30000L),
                    createOrderProduct(pricePolicyId, 10L, 2, 30000L)
            ));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            ProductInfoResult productInfo = new ProductInfoResult(
                    1L, null, "테스트 상품", "테스트 브랜드", null, null, null, List.of()
            );
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(pricePolicyId, productInfo));

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderProducts()).hasSize(2);
            assertThat(result.orderProducts()).allMatch(p -> p.productName().equals("테스트 상품"));
        }

        @Test
        @DisplayName("주문 상태가 CANCELLED인 경우도 정상 조회된다")
        void getOrderAndOrderProducts_cancelledOrder_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.CANCELLED, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("주문 상태가 REFUNDED인 경우도 정상 조회된다")
        void getOrderAndOrderProducts_refundedOrder_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.REFUNDED, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());

            GetOrderResult result = getOrderService.getOrderAndOrderProducts(orderId);

            assertThat(result.orderStatus()).isEqualTo(OrderStatus.REFUNDED);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrder(Long orderId, List<OrderProduct> orderProducts) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(orderProducts.stream()
                        .map(this::toSnapshotState)
                        .toList())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithBuyerId(Long orderId, Long buyerId, List<OrderProduct> orderProducts) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(orderProducts.stream()
                        .map(this::toSnapshotState)
                        .toList())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithOrderNumber(Long orderId, String orderNumber, List<OrderProduct> orderProducts) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber(orderNumber)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(orderProducts.stream()
                        .map(this::toSnapshotState)
                        .toList())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithStatus(Long orderId, OrderStatus status, List<OrderProduct> orderProducts) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(status)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(orderProducts.stream()
                        .map(this::toSnapshotState)
                        .toList())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithStatusChangeReason(
            Long orderId,
            OrderStatusReasonCategory reasonCategory,
            String reason,
            List<OrderProduct> orderProducts
    ) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.CANCELLED)
                .statusChangeReasonCategory(reasonCategory)
                .statusChangeReason(reason)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(orderProducts.stream()
                        .map(this::toSnapshotState)
                        .toList())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithAmounts(
            Long orderId,
            Long totalAmount,
            Long paidAmount,
            Long couponAmount,
            Long pointAmount,
            List<OrderProduct> orderProducts
    ) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(totalAmount, paidAmount, couponAmount, pointAmount, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(orderProducts.stream()
                        .map(this::toSnapshotState)
                        .toList())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithProductStatus(Long orderId, Long pricePolicyId, OrderStatus productStatus) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of(
                        OrderProductSnapshotState.builder()
                                .orderId(orderId)
                                .sellerId(10L)
                                .pricePolicyId(pricePolicyId)
                                .quantity(1)
                                .unitAmount(50000L)
                                .orderStatus(productStatus)
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithVariousProductStatuses(Long orderId) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PARTIALLY_REFUNDED)
                .amount(OrderAmount.of(300000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of(
                        OrderProductSnapshotState.builder()
                                .orderId(orderId)
                                .sellerId(10L)
                                .pricePolicyId(100L)
                                .quantity(1)
                                .unitAmount(100000L)
                                .orderStatus(OrderStatus.CONFIRMED)
                                .build(),
                        OrderProductSnapshotState.builder()
                                .orderId(orderId)
                                .sellerId(20L)
                                .pricePolicyId(200L)
                                .quantity(1)
                                .unitAmount(100000L)
                                .orderStatus(OrderStatus.REFUNDED)
                                .build(),
                        OrderProductSnapshotState.builder()
                                .orderId(orderId)
                                .sellerId(30L)
                                .pricePolicyId(300L)
                                .quantity(1)
                                .unitAmount(100000L)
                                .orderStatus(OrderStatus.SHIPPING)
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private OrderProduct createOrderProduct(Long pricePolicyId, Long sellerId, Integer quantity, Long unitAmount) {
        return OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(sellerId)
                .pricePolicyId(pricePolicyId)
                .quantity(quantity)
                .unitAmount(unitAmount)
                .orderStatus(OrderStatus.PAID)
                .build());
    }

    private OrderProduct createOrderProductWithSellerId(Long pricePolicyId, Long sellerId, Integer quantity, Long unitAmount) {
        return OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(sellerId)
                .pricePolicyId(pricePolicyId)
                .quantity(quantity)
                .unitAmount(unitAmount)
                .orderStatus(OrderStatus.PAID)
                .build());
    }

    private OrderProduct createOrderProductWithSharerId(Long pricePolicyId, Long sellerId, Long sharerId, Integer quantity, Long unitAmount) {
        return OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(sellerId)
                .pricePolicyId(pricePolicyId)
                .sharerId(sharerId)
                .quantity(quantity)
                .unitAmount(unitAmount)
                .orderStatus(OrderStatus.PAID)
                .build());
    }

    private OrderProduct createOrderProductWithImageUrl(Long pricePolicyId, Long sellerId, Integer quantity, Long unitAmount, String imageUrl) {
        return OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(sellerId)
                .pricePolicyId(pricePolicyId)
                .quantity(quantity)
                .unitAmount(unitAmount)
                .imageUrl(imageUrl)
                .orderStatus(OrderStatus.PAID)
                .build());
    }

    private OrderProduct createOrderProductWithReviewStatus(Long pricePolicyId, Long sellerId, Integer quantity, Long unitAmount, Boolean isReviewed) {
        return OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(sellerId)
                .pricePolicyId(pricePolicyId)
                .quantity(quantity)
                .unitAmount(unitAmount)
                .orderStatus(OrderStatus.CONFIRMED)
                .isReviewed(isReviewed)
                .build());
    }

    private OrderProductSnapshotState toSnapshotState(OrderProduct orderProduct) {
        return OrderProductSnapshotState.builder()
                .orderId(orderProduct.getOrderId())
                .sellerId(orderProduct.getSellerId())
                .pricePolicyId(orderProduct.getPricePolicyId())
                .sharerId(orderProduct.getSharerId())
                .quantity(orderProduct.getQuantity())
                .unitAmount(orderProduct.getUnitAmount())
                .imageUrl(orderProduct.getImageUrl())
                .orderStatus(orderProduct.getOrderStatus())
                .isReviewed(orderProduct.getIsReviewed())
                .build();
    }
}
