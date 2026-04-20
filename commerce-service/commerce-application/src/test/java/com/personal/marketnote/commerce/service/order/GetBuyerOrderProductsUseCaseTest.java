package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderProductsQuery;
import com.personal.marketnote.commerce.port.in.result.order.GetBuyerOrderProductResult;
import com.personal.marketnote.commerce.port.in.result.order.GetBuyerOrderProductsResult;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderProductPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBuyerOrderProductsUseCaseTest {
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

    @Captor
    private ArgumentCaptor<Long> buyerIdCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> startDateCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> endDateCaptor;
    @Captor
    private ArgumentCaptor<List<OrderStatus>> statusesCaptor;
    @Captor
    private ArgumentCaptor<List<Long>> pricePolicyIdsCaptor;

    // ==================================================================================
    // 기본 조회 성공 케이스
    // ==================================================================================

    @Nested
    @DisplayName("구매자 주문 상품 목록 조회 성공 케이스")
    class GetBuyerOrderProductsSuccessTest {

        @Test
        @DisplayName("주문이 존재하면 주문 상품 목록을 반환한다")
        void returnsOrderProductsWhenOrdersExist() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, true)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
        }

        @Test
        @DisplayName("여러 주문의 상품들을 하나의 목록으로 반환한다")
        void returnsAllProductsFromMultipleOrdersAsFlatList() {
            Long buyerId = 1L;
            Order order1 = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, true),
                    productState(200L, false)
            ));
            Order order2 = createOrderWithReviewedProducts(2L, buyerId, List.of(
                    productState(300L, true)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C")
                    ));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(3);
        }

        @Test
        @DisplayName("반환된 결과의 orderProducts는 null이 아니다")
        void resultOrderProductsIsNotNull() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isNotNull();
        }
    }

    // ==================================================================================
    // 빈 결과 케이스
    // ==================================================================================

    @Nested
    @DisplayName("빈 결과 케이스")
    class EmptyResultTest {

        @Test
        @DisplayName("findOrderPort가 빈 리스트를 반환하면 빈 상품 목록을 반환한다")
        void returnsEmptyWhenPortReturnsEmptyList() {
            Long buyerId = 1L;
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of());

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("findOrderPort가 null을 반환하면 빈 상품 목록을 반환한다")
        void returnsEmptyWhenPortReturnsNull() {
            Long buyerId = 1L;
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(null);

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }
    }

    // ==================================================================================
    // 리뷰 상태 필터 케이스
    // ==================================================================================

    @Nested
    @DisplayName("리뷰 상태 필터 케이스")
    class ReviewStatusFilterTest {

        @Test
        @DisplayName("isReviewed가 null이면 모든 주문 상품을 반환한다")
        void returnsAllProductsWhenIsReviewedIsNull() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, true),
                    productState(200L, false),
                    productState(300L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C")
                    ));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(3);
        }

        @Test
        @DisplayName("isReviewed가 true이면 리뷰 작성된 상품만 반환한다")
        void returnsOnlyReviewedProductsWhenIsReviewedIsTrue() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, true),
                    productState(200L, false),
                    productState(300L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, true);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("isReviewed가 false이면 리뷰 미작성 상품만 반환한다")
        void returnsOnlyUnreviewedProductsWhenIsReviewedIsFalse() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, true),
                    productState(200L, false),
                    productState(300L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, false);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C")
                    ));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(2);
            assertThat(result.orderProducts())
                    .extracting(GetBuyerOrderProductResult::pricePolicyId)
                    .containsExactlyInAnyOrder(200L, 300L);
        }

        @Test
        @DisplayName("isReviewed가 true인데 리뷰 작성된 상품이 없으면 빈 목록을 반환한다")
        void returnsEmptyWhenIsReviewedTrueButNoReviewedProducts() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, false),
                    productState(200L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, true);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("isReviewed가 false이면 isReviewed가 null인 상품도 포함된다")
        void includesNullReviewedProductsWhenIsReviewedIsFalse() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null),
                    productState(200L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, false);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B")
                    ));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(2);
        }

        @Test
        @DisplayName("isReviewed가 false이면 isReviewed가 false인 상품도 포함된다")
        void includesFalseReviewedProductsWhenIsReviewedIsFalse() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, false)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, false);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).isReviewed()).isEqualTo(false);
        }

        @Test
        @DisplayName("여러 주문에서 리뷰 필터가 일관되게 적용된다")
        void reviewFilterAppliesToAllOrders() {
            Long buyerId = 1L;
            Order order1 = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, true),
                    productState(200L, false)
            ));
            Order order2 = createOrderWithReviewedProducts(2L, buyerId, List.of(
                    productState(300L, true),
                    productState(400L, false)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, true);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            300L, createProductInfo(300L, "상품C")
                    ));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(2);
            assertThat(result.orderProducts())
                    .extracting(GetBuyerOrderProductResult::pricePolicyId)
                    .containsExactlyInAnyOrder(100L, 300L);
        }
    }

    // ==================================================================================
    // 결과 매핑 검증
    // ==================================================================================

    @Nested
    @DisplayName("결과 매핑 검증")
    class ResultMappingTest {

        @Test
        @DisplayName("반환된 결과의 orderId가 올바르게 매핑된다")
        void mapsOrderIdCorrectly() {
            Long buyerId = 1L;
            Long orderId = 42L;
            Order order = createOrderWithReviewedProducts(orderId, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).orderId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("반환된 결과의 orderNumber가 올바르게 매핑된다")
        void mapsOrderNumberCorrectly() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).orderNumber()).isEqualTo("ORD-1");
        }

        @Test
        @DisplayName("반환된 결과의 orderDate가 주문 생성일에서 LocalDate로 변환된다")
        void mapsOrderDateFromCreatedAt() {
            Long buyerId = 1L;
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
            Order order = createOrderWithTimestamp(1L, buyerId, List.of(
                    productState(100L, null)
            ), createdAt);
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).orderDate())
                    .isEqualTo(createdAt.toLocalDate());
        }

        @Test
        @DisplayName("반환된 결과의 pricePolicyId가 올바르게 매핑된다")
        void mapsPricePolicyIdCorrectly() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(777L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(777L, createProductInfo(777L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(777L);
        }

        @Test
        @DisplayName("반환된 결과의 quantity가 올바르게 매핑된다")
        void mapsQuantityCorrectly() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithQuantity(100L, null, 5)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).quantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("반환된 결과의 productName이 상품 정보에서 매핑된다")
        void mapsProductNameFromProductInfo() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "나이키 에어맥스 90")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).productName()).isEqualTo("나이키 에어맥스 90");
        }

        @Test
        @DisplayName("반환된 결과의 brandName이 상품 정보에서 매핑된다")
        void mapsBrandNameFromProductInfo() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, new ProductInfoResult(100L, null, "상품A", "나이키", null, null, null, List.of())));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).brandName()).isEqualTo("나이키");
        }

        @Test
        @DisplayName("상품 정보가 없으면 productName이 null이다")
        void productNameIsNullWhenProductInfoNotFound() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(999L, createProductInfo(999L, "다른상품")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).productName()).isNull();
        }

        @Test
        @DisplayName("상품 정보가 없으면 brandName이 null이다")
        void brandNameIsNullWhenProductInfoNotFound() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(999L, createProductInfo(999L, "다른상품")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).brandName()).isNull();
        }

        @Test
        @DisplayName("상품 정보가 없으면 productId가 null이다")
        void productIdIsNullWhenProductInfoNotFound() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(999L, createProductInfo(999L, "다른상품")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).productId()).isNull();
        }

        @Test
        @DisplayName("반환된 결과의 isReviewed가 올바르게 매핑된다")
        void mapsIsReviewedCorrectly() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, true)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).isReviewed()).isTrue();
        }

        @Test
        @DisplayName("구매 확정일이 존재하면 reviewDeadline이 구매 확정일로부터 30일 후로 매핑된다")
        void mapsReviewDeadlineFromConfirmedAt() {
            Long buyerId = 1L;
            LocalDateTime confirmedAt = LocalDateTime.of(2026, 2, 1, 10, 0, 0);
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, confirmedAt)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).reviewDeadline())
                    .isEqualTo(confirmedAt.plusDays(30));
        }

        @Test
        @DisplayName("구매 확정일이 null이면 reviewDeadline이 null로 매핑된다")
        void reviewDeadlineIsNullWhenConfirmedAtIsNull() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).reviewDeadline()).isNull();
        }

        @Test
        @DisplayName("여러 상품의 구매 확정일이 다르면 각각의 reviewDeadline이 개별적으로 계산된다")
        void mapsReviewDeadlineIndividuallyForEachProduct() {
            Long buyerId = 1L;
            LocalDateTime confirmedAt1 = LocalDateTime.of(2026, 2, 1, 10, 0, 0);
            LocalDateTime confirmedAt2 = LocalDateTime.of(2026, 2, 15, 14, 0, 0);
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, confirmedAt1),
                    productStateWithConfirmedAt(200L, confirmedAt2)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B")
                    ));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(2);
            assertThat(result.orderProducts().get(0).reviewDeadline())
                    .isEqualTo(confirmedAt1.plusDays(30));
            assertThat(result.orderProducts().get(1).reviewDeadline())
                    .isEqualTo(confirmedAt2.plusDays(30));
        }
    }

    // ==================================================================================
    // 주문 상태 매핑 검증
    // ==================================================================================

    @Nested
    @DisplayName("주문 상태 매핑 검증")
    class OrderStatusMappingTest {

        @Test
        @DisplayName("주문 상품의 orderStatus가 CONFIRMED이면 그대로 매핑된다")
        void mapsOrderProductStatusDirectly() {
            Long buyerId = 1L;
            Order order = createOrderWithProductStatus(1L, buyerId, 100L, OrderStatus.CONFIRMED, OrderStatus.CONFIRMED);
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts().get(0).orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }
    }

    // ==================================================================================
    // 구매 확정 필터링 검증
    // ==================================================================================

    @Nested
    @DisplayName("구매 확정 필터링 검증")
    class ConfirmedStatusFilterTest {

        @Test
        @DisplayName("주문 상품의 orderStatus가 CONFIRMED이면 결과에 포함된다")
        void includesConfirmedOrderProduct() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, null, OrderStatus.CONFIRMED)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
        }

        @Test
        @DisplayName("주문 상품의 orderStatus가 PAID이면 결과에서 제외된다")
        void excludesPaidOrderProduct() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, null, OrderStatus.PAID)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("주문 상품의 orderStatus가 DELIVERED이면 결과에서 제외된다")
        void excludesDeliveredOrderProduct() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, null, OrderStatus.DELIVERED)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("주문 상품의 orderStatus가 SHIPPING이면 결과에서 제외된다")
        void excludesShippingOrderProduct() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, null, OrderStatus.SHIPPING)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("주문 상품의 orderStatus가 CANCELLED이면 결과에서 제외된다")
        void excludesCancelledOrderProduct() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, null, OrderStatus.CANCELLED)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("주문 상품의 orderStatus가 RETURNED이면 결과에서 제외된다")
        void excludesReturnedOrderProduct() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, null, OrderStatus.RETURNED)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("CONFIRMED 상품과 비 CONFIRMED 상품이 혼재하면 CONFIRMED만 반환한다")
        void returnsOnlyConfirmedWhenMixed() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, null, OrderStatus.CONFIRMED),
                    productStateWithStatus(200L, null, OrderStatus.PAID),
                    productStateWithStatus(300L, null, OrderStatus.SHIPPING)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("여러 주문에서 CONFIRMED 필터가 일관되게 적용된다")
        void confirmedFilterAppliesToAllOrders() {
            Long buyerId = 1L;
            Order order1 = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, null, OrderStatus.CONFIRMED),
                    productStateWithStatus(200L, null, OrderStatus.PAID)
            ));
            Order order2 = createOrderWithReviewedProducts(2L, buyerId, List.of(
                    productStateWithStatus(300L, null, OrderStatus.SHIPPING),
                    productStateWithStatus(400L, null, OrderStatus.CONFIRMED)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            400L, createProductInfo(400L, "상품D")
                    ));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(2);
            assertThat(result.orderProducts())
                    .extracting(GetBuyerOrderProductResult::pricePolicyId)
                    .containsExactlyInAnyOrder(100L, 400L);
        }

        @Test
        @DisplayName("CONFIRMED 필터와 isReviewed 필터가 동시에 적용된다")
        void confirmedAndReviewFilterApplyTogether() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, false, OrderStatus.CONFIRMED),
                    productStateWithStatus(200L, true, OrderStatus.CONFIRMED),
                    productStateWithStatus(300L, false, OrderStatus.PAID)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, false);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("모든 주문 상품이 CONFIRMED가 아니면 빈 목록을 반환한다")
        void returnsEmptyWhenNoConfirmedProducts() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, null, OrderStatus.PAID),
                    productStateWithStatus(200L, null, OrderStatus.SHIPPING),
                    productStateWithStatus(300L, null, OrderStatus.DELIVERED)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("주문 상품의 orderStatus가 null이면 결과에서 제외된다")
        void excludesNullStatusOrderProduct() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithStatus(100L, null, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }
    }

    // ==================================================================================
    // 포트 호출 검증
    // ==================================================================================

    @Nested
    @DisplayName("포트 호출 검증")
    class PortInvocationTest {

        @Test
        @DisplayName("findOrderPort.findByBuyerId를 buyerId, null, null, 빈 리스트로 호출한다")
        void callsFindOrderPortWithCorrectParameters() {
            Long buyerId = 42L;
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderProducts(query);

            verify(findOrderPort).findByBuyerId(
                    buyerIdCaptor.capture(),
                    startDateCaptor.capture(),
                    endDateCaptor.capture(),
                    statusesCaptor.capture()
            );
            assertThat(buyerIdCaptor.getValue()).isEqualTo(42L);
            assertThat(startDateCaptor.getValue()).isNull();
            assertThat(endDateCaptor.getValue()).isNull();
            assertThat(statusesCaptor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("findOrderPort.findByBuyerId를 정확히 한 번 호출한다")
        void callsFindOrderPortExactlyOnce() {
            Long buyerId = 1L;
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderProducts(query);

            verify(findOrderPort, times(1)).findByBuyerId(
                    eq(buyerId), isNull(), isNull(), eq(List.of())
            );
        }

        @Test
        @DisplayName("주문이 존재하면 findProductByPricePolicyPort를 호출한다")
        void callsProductPortWhenOrdersExist() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            getOrderService.getBuyerOrderProducts(query);

            verify(findProductByPricePolicyPort, times(1)).findByPricePolicyIds(anyList());
        }

        @Test
        @DisplayName("주문이 없으면 findProductByPricePolicyPort를 호출하지 않는다")
        void doesNotCallProductPortWhenNoOrders() {
            Long buyerId = 1L;
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderProducts(query);

            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("findOrderProductPort와 상호작용하지 않는다")
        void doesNotInteractWithFindOrderProductPort() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            getOrderService.getBuyerOrderProducts(query);

            verifyNoInteractions(findOrderProductPort);
        }

        @Test
        @DisplayName("리뷰 필터로 걸러진 상품의 pricePolicyId는 상품 정보 조회에서 제외된다")
        void excludesFilteredProductPricePolicyIdsFromProductLookup() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, true),
                    productState(200L, false),
                    productState(300L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, true);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            getOrderService.getBuyerOrderProducts(query);

            verify(findProductByPricePolicyPort).findByPricePolicyIds(pricePolicyIdsCaptor.capture());
            assertThat(pricePolicyIdsCaptor.getValue()).containsExactly(100L);
            assertThat(pricePolicyIdsCaptor.getValue()).doesNotContain(200L, 300L);
        }

        @Test
        @DisplayName("pricePolicyId 중복이면 distinct하여 조회한다")
        void deduplicatesPricePolicyIds() {
            Long buyerId = 1L;
            Order order1 = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            Order order2 = createOrderWithReviewedProducts(2L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            getOrderService.getBuyerOrderProducts(query);

            verify(findProductByPricePolicyPort).findByPricePolicyIds(pricePolicyIdsCaptor.capture());
            assertThat(pricePolicyIdsCaptor.getValue()).hasSize(1);
            assertThat(pricePolicyIdsCaptor.getValue()).containsExactly(100L);
        }
    }

    // ==================================================================================
    // 리뷰 작성기한 필터 검증
    // ==================================================================================

    @Nested
    @DisplayName("리뷰 작성기한 필터 검증")
    class ReviewDeadlineFilterTest {

        // Clock = 2026-03-01T00:00:00Z → Asia/Seoul = 2026-03-01T09:00:00
        private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 1, 9, 0, 0);
        private static final LocalDateTime CONFIRMED_29_DAYS_AGO = NOW.minusDays(29);
        private static final LocalDateTime CONFIRMED_30_DAYS_AGO = NOW.minusDays(30);
        private static final LocalDateTime CONFIRMED_31_DAYS_AGO = NOW.minusDays(31);

        @Test
        @DisplayName("구매 확정 후 29일이면 결과에 포함된다")
        void withinDeadline29DaysIncluded() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, CONFIRMED_29_DAYS_AGO)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);
            ProductInfoResult productInfo = createProductInfo(1L, "상품A");

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(100L)))
                    .thenReturn(Map.of(100L, productInfo));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
        }

        @Test
        @DisplayName("구매 확정 후 정확히 30일이면 결과에 포함된다")
        void exactlyOnDeadlineIncluded() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, CONFIRMED_30_DAYS_AGO)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);
            ProductInfoResult productInfo = createProductInfo(1L, "상품A");

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(100L)))
                    .thenReturn(Map.of(100L, productInfo));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
        }

        @Test
        @DisplayName("구매 확정 후 31일이면 결과에서 제외된다")
        void pastDeadline31DaysExcluded() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, CONFIRMED_31_DAYS_AGO)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("confirmedAt이 null이면 기한 내로 간주하여 결과에 포함된다")
        void nullConfirmedAtTreatedAsWithinDeadline() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);
            ProductInfoResult productInfo = createProductInfo(1L, "상품A");

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(100L)))
                    .thenReturn(Map.of(100L, productInfo));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
        }

        @Test
        @DisplayName("기한 내 상품과 기한 만료 상품이 혼재하면 기한 내만 반환한다")
        void mixedDeadlineReturnsOnlyWithin() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, CONFIRMED_29_DAYS_AGO),
                    productStateWithConfirmedAt(200L, CONFIRMED_31_DAYS_AGO)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);
            ProductInfoResult productInfo = createProductInfo(1L, "상품A");

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(100L)))
                    .thenReturn(Map.of(100L, productInfo));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("여러 주문에서 기한 필터가 일관되게 적용된다")
        void deadlineFilterAppliedConsistentlyAcrossOrders() {
            Long buyerId = 1L;
            Order order1 = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, CONFIRMED_29_DAYS_AGO)
            ));
            Order order2 = createOrderWithReviewedProducts(2L, buyerId, List.of(
                    productStateWithConfirmedAt(200L, CONFIRMED_31_DAYS_AGO)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);
            ProductInfoResult productInfo = createProductInfo(1L, "상품A");

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(100L)))
                    .thenReturn(Map.of(100L, productInfo));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("기한 필터와 리뷰 필터가 동시에 적용된다")
        void deadlineAndReviewFilterAppliedTogether() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    new ProductStateInput(100L, true, 1, OrderStatus.CONFIRMED, CONFIRMED_29_DAYS_AGO),
                    new ProductStateInput(200L, null, 1, OrderStatus.CONFIRMED, CONFIRMED_29_DAYS_AGO)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, false);
            ProductInfoResult productInfo200 = createProductInfo(2L, "상품B");

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(200L)))
                    .thenReturn(Map.of(200L, productInfo200));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).pricePolicyId()).isEqualTo(200L);
        }

        @Test
        @DisplayName("모든 상품이 기한 만료이면 빈 목록을 반환한다")
        void allExpiredReturnsEmpty() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, CONFIRMED_31_DAYS_AGO),
                    productStateWithConfirmedAt(200L, CONFIRMED_31_DAYS_AGO)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("기한 만료 상품의 pricePolicyId는 상품 정보 조회에서 제외된다")
        void expiredProductPricePolicyIdExcludedFromQuery() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productStateWithConfirmedAt(100L, CONFIRMED_29_DAYS_AGO),
                    productStateWithConfirmedAt(200L, CONFIRMED_31_DAYS_AGO)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);
            ProductInfoResult productInfo = createProductInfo(1L, "상품A");

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(pricePolicyIdsCaptor.capture()))
                    .thenReturn(Map.of(100L, productInfo));

            getOrderService.getBuyerOrderProducts(query);

            assertThat(pricePolicyIdsCaptor.getValue()).containsExactly(100L);
        }
    }

    // ==================================================================================
    // 엣지 케이스
    // ==================================================================================

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCaseTest {

        @Test
        @DisplayName("주문 상품이 빈 목록인 주문은 결과에 상품이 포함되지 않는다")
        void orderWithEmptyProductsAddsNoResults() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of());
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }

        @Test
        @DisplayName("findProductByPricePolicyPort가 null을 반환하면 빈 맵으로 처리한다")
        void handlesNullReturnFromProductPort() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(null);

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(1);
            assertThat(result.orderProducts().get(0).productName()).isNull();
        }

        @Test
        @DisplayName("여러 주문에 각각 여러 상품이 있으면 모두 결과에 포함된다")
        void includesAllProductsFromMultipleOrdersWithMultipleProducts() {
            Long buyerId = 1L;
            Order order1 = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, null),
                    productState(200L, null)
            ));
            Order order2 = createOrderWithReviewedProducts(2L, buyerId, List.of(
                    productState(300L, null),
                    productState(400L, null),
                    productState(500L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C"),
                            400L, createProductInfo(400L, "상품D"),
                            500L, createProductInfo(500L, "상품E")
                    ));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).hasSize(5);
        }

        @Test
        @DisplayName("buyerId가 다른 값이면 각각 올바른 buyerId로 호출한다")
        void passesCorrectBuyerIdForDifferentBuyers() {
            Long buyerId1 = 10L;
            Long buyerId2 = 20L;
            GetBuyerOrderProductsQuery query1 = createQuery(buyerId1, null);
            GetBuyerOrderProductsQuery query2 = createQuery(buyerId2, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderProducts(query1);
            getOrderService.getBuyerOrderProducts(query2);

            verify(findOrderPort).findByBuyerId(eq(10L), isNull(), isNull(), eq(List.of()));
            verify(findOrderPort).findByBuyerId(eq(20L), isNull(), isNull(), eq(List.of()));
        }

        @Test
        @DisplayName("리뷰 필터로 모든 상품이 제거되면 빈 목록을 반환한다")
        void returnsEmptyWhenReviewFilterRemovesAllProducts() {
            Long buyerId = 1L;
            Order order = createOrderWithReviewedProducts(1L, buyerId, List.of(
                    productState(100L, false),
                    productState(200L, null)
            ));
            GetBuyerOrderProductsQuery query = createQuery(buyerId, true);

            when(findOrderPort.findByBuyerId(eq(buyerId), isNull(), isNull(), eq(List.of())))
                    .thenReturn(List.of(order));

            GetBuyerOrderProductsResult result = getOrderService.getBuyerOrderProducts(query);

            assertThat(result.orderProducts()).isEmpty();
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private GetBuyerOrderProductsQuery createQuery(Long buyerId, Boolean isReviewed) {
        return GetBuyerOrderProductsQuery.of(buyerId, isReviewed);
    }

    private record ProductStateInput(Long pricePolicyId, Boolean isReviewed, Integer quantity,
                                     OrderStatus orderStatus, LocalDateTime confirmedAt) {
    }

    private ProductStateInput productState(Long pricePolicyId, Boolean isReviewed) {
        return new ProductStateInput(pricePolicyId, isReviewed, 1, OrderStatus.CONFIRMED, null);
    }

    private ProductStateInput productStateWithQuantity(Long pricePolicyId, Boolean isReviewed, Integer quantity) {
        return new ProductStateInput(pricePolicyId, isReviewed, quantity, OrderStatus.CONFIRMED, null);
    }

    private ProductStateInput productStateWithStatus(Long pricePolicyId, Boolean isReviewed, OrderStatus status) {
        return new ProductStateInput(pricePolicyId, isReviewed, 1, status, null);
    }

    private ProductStateInput productStateWithConfirmedAt(Long pricePolicyId, LocalDateTime confirmedAt) {
        return new ProductStateInput(pricePolicyId, null, 1, OrderStatus.CONFIRMED, confirmedAt);
    }

    private Order createOrderWithReviewedProducts(Long orderId, Long buyerId, List<ProductStateInput> productInputs) {
        List<OrderProductSnapshotState> productStates = productInputs.stream()
                .map(input -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(input.pricePolicyId())
                        .quantity(input.quantity())
                        .unitAmount(50000L)
                        .imageUrl("https://img.example.com/" + input.pricePolicyId())
                        .orderStatus(input.orderStatus())
                        .isReviewed(input.isReviewed())
                        .confirmedAt(input.confirmedAt())
                        .build())
                .toList();

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithTimestamp(
            Long orderId, Long buyerId, List<ProductStateInput> productInputs, LocalDateTime createdAt
    ) {
        List<OrderProductSnapshotState> productStates = productInputs.stream()
                .map(input -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(input.pricePolicyId())
                        .quantity(input.quantity())
                        .unitAmount(50000L)
                        .orderStatus(input.orderStatus())
                        .isReviewed(input.isReviewed())
                        .build())
                .toList();

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(createdAt)
                .modifiedAt(createdAt)
                .build());
    }

    private Order createOrderWithProductStatus(
            Long orderId, Long buyerId, Long pricePolicyId,
            OrderStatus productStatus, OrderStatus orderStatus
    ) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(productStatus)
                        .isReviewed(null)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(orderStatus)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private ProductInfoResult createProductInfo(Long id, String name) {
        return new ProductInfoResult(id, null, name, "브랜드", null, null, null, List.of());
    }
}
