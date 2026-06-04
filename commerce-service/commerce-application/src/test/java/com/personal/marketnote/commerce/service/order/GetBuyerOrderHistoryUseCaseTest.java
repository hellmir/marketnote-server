package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderHistoryQuery;
import com.personal.marketnote.commerce.port.in.result.order.GetBuyerOrdersResult;
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

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBuyerOrderHistoryUseCaseTest {
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
    @DisplayName("구매자 주문 내역 조회 성공 케이스")
    class GetBuyerOrderHistorySuccessTest {

        @Test
        @DisplayName("주문이 존재하면 주문 목록을 반환한다")
        void returnsOrdersWhenOrdersExist() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(1);
            assertThat(result.orders().get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("여러 주문이 존재하면 모든 주문을 반환한다")
        void returnsAllOrdersWhenMultipleOrdersExist() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            Order order3 = createOrderWithProducts(3L, buyerId, List.of(300L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2, order3));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C")
                    ));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(3);
        }

        @Test
        @DisplayName("반환된 결과의 orderedProducts에 pricePolicyId별 상품 정보가 포함된다")
        void resultContainsProductInfoByPricePolicyId() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L, 200L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            Map<Long, ProductInfoResult> productInfoMap = Map.of(
                    100L, createProductInfo(100L, "상품A"),
                    200L, createProductInfo(200L, "상품B")
            );

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(productInfoMap);

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orderedProducts()).containsKey(100L);
            assertThat(result.orderedProducts()).containsKey(200L);
            assertThat(result.orderedProducts().get(100L).name()).isEqualTo("상품A");
            assertThat(result.orderedProducts().get(200L).name()).isEqualTo("상품B");
        }

        @Test
        @DisplayName("반환된 결과의 orders는 null이 아니다")
        void resultOrdersIsNotNull() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).isNotNull();
        }

        @Test
        @DisplayName("반환된 결과의 orderedProducts는 null이 아니다")
        void resultOrderedProductsIsNotNull() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orderedProducts()).isNotNull();
        }
    }

    // ==================================================================================
    // 빈 결과 케이스
    // ==================================================================================

    @Nested
    @DisplayName("빈 결과 케이스")
    class EmptyResultTest {

        @Test
        @DisplayName("findOrderPort가 빈 리스트를 반환하면 빈 주문 목록을 반환한다")
        void returnsEmptyOrdersWhenPortReturnsEmptyList() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of());

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).isEmpty();
        }

        @Test
        @DisplayName("findOrderPort가 빈 리스트를 반환하면 빈 상품 맵을 반환한다")
        void returnsEmptyProductsWhenPortReturnsEmptyList() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of());

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orderedProducts()).isEmpty();
        }

        @Test
        @DisplayName("findOrderPort가 null을 반환하면 빈 주문 목록을 반환한다")
        void returnsEmptyOrdersWhenPortReturnsNull() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(null);

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).isEmpty();
        }

        @Test
        @DisplayName("findOrderPort가 null을 반환하면 빈 상품 맵을 반환한다")
        void returnsEmptyProductsWhenPortReturnsNull() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(null);

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orderedProducts()).isEmpty();
        }
    }

    // ==================================================================================
    // 상품명 필터 케이스
    // ==================================================================================

    @Nested
    @DisplayName("상품명 필터 케이스")
    class ProductNameFilterTest {

        @Test
        @DisplayName("상품명이 null이면 모든 주문을 반환한다")
        void returnsAllOrdersWhenProductNameIsNull() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B")
                    ));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(2);
        }

        @Test
        @DisplayName("상품명이 빈 문자열이면 모든 주문을 반환한다")
        void returnsAllOrdersWhenProductNameIsEmpty() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B")
                    ));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(2);
        }

        @Test
        @DisplayName("상품명이 공백 문자열이면 모든 주문을 반환한다")
        void returnsAllOrdersWhenProductNameIsBlank() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "   ");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B")
                    ));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(2);
        }

        @Test
        @DisplayName("상품명과 정확히 일치하는 상품이 있는 주문을 반환한다")
        void returnsOrdersWithExactProductNameMatch() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "나이키 에어맥스");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "나이키 에어맥스")));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(1);
        }

        @Test
        @DisplayName("상품명과 부분 일치하는 상품이 있는 주문을 반환한다")
        void returnsOrdersWithPartialProductNameMatch() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "나이키");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "나이키 에어맥스 90")));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(1);
        }

        @Test
        @DisplayName("상품명 필터링은 대소문자를 구분하지 않는다")
        void productNameFilterIsCaseInsensitive() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "NIKE");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "Nike Air Max")));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(1);
        }

        @Test
        @DisplayName("상품명과 일치하는 상품이 없으면 빈 목록을 반환한다")
        void returnsEmptyWhenNoProductNameMatch() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "아디다스");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "나이키 에어맥스")));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).isEmpty();
        }

        @Test
        @DisplayName("여러 주문 중 상품명과 일치하는 주문만 반환한다")
        void returnsOnlyMatchingOrdersFromMultipleOrders() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            Order order3 = createOrderWithProducts(3L, buyerId, List.of(300L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "나이키");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2, order3));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "나이키 에어맥스"),
                            200L, createProductInfo(200L, "아디다스 울트라부스트"),
                            300L, createProductInfo(300L, "나이키 덩크")
                    ));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(2);
            assertThat(result.orders())
                    .extracting(Order::getId)
                    .containsExactly(1L, 3L);
        }

        @Test
        @DisplayName("상품명 앞뒤 공백은 trim 후 필터링된다")
        void productNameIsTrimmedBeforeFiltering() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "  나이키  ");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "나이키 에어맥스")));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(1);
        }

        @Test
        @DisplayName("상품 정보 맵에 pricePolicyId가 없으면 해당 주문이 필터링된다")
        void filtersOutOrderWhenProductInfoNotFoundForPricePolicyId() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품A");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(999L, createProductInfo(999L, "상품A")));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).isEmpty();
        }

        @Test
        @DisplayName("상품 정보의 name이 null이면 해당 주문이 필터링된다")
        void filtersOutOrderWhenProductNameIsNullInProductInfo() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품A");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, null)));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).isEmpty();
        }
    }

    // ==================================================================================
    // 쿼리 파라미터 전달 검증
    // ==================================================================================

    @Nested
    @DisplayName("쿼리 파라미터 전달 검증")
    class QueryParameterPassingTest {

        @Test
        @DisplayName("buyerId를 정확히 findOrderPort에 전달한다")
        void passesBuyerIdToFindOrderPort() {
            Long buyerId = 42L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    buyerIdCaptor.capture(), any(), any(), anyList()
            );
            assertThat(buyerIdCaptor.getValue()).isEqualTo(42L);
        }

        @Test
        @DisplayName("endDate는 내일 자정으로 전달된다")
        void passesTomorrowMidnightAsEndDate() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), endDateCaptor.capture(), anyList()
            );
            LocalDate today = LocalDate.now();
            LocalDateTime expectedEndDate = LocalDateTime.of(today.plusDays(1), LocalTime.MIDNIGHT);
            assertThat(endDateCaptor.getValue()).isEqualTo(expectedEndDate);
        }

        @Test
        @DisplayName("period가 null이면 startDate가 null로 전달된다")
        void passesNullStartDateWhenPeriodIsNull() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            assertThat(startDateCaptor.getValue()).isNull();
        }

        @Test
        @DisplayName("period가 ONE_MONTH이면 1개월 전 자정이 startDate로 전달된다")
        void passesOneMonthAgoStartDateWhenPeriodIsOneMonth() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, OrderPeriod.ONE_MONTH, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            LocalDate today = LocalDate.now();
            LocalDateTime expectedStartDate = LocalDateTime.of(today.minusMonths(1), LocalTime.MIDNIGHT);
            assertThat(startDateCaptor.getValue()).isEqualTo(expectedStartDate);
        }

        @Test
        @DisplayName("period가 THREE_MONTHS이면 3개월 전 자정이 startDate로 전달된다")
        void passesThreeMonthsAgoStartDateWhenPeriodIsThreeMonths() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, OrderPeriod.THREE_MONTHS, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            LocalDate today = LocalDate.now();
            LocalDateTime expectedStartDate = LocalDateTime.of(today.minusMonths(3), LocalTime.MIDNIGHT);
            assertThat(startDateCaptor.getValue()).isEqualTo(expectedStartDate);
        }

        @Test
        @DisplayName("period가 SIX_MONTHS이면 6개월 전 자정이 startDate로 전달된다")
        void passesSixMonthsAgoStartDateWhenPeriodIsSixMonths() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, OrderPeriod.SIX_MONTHS, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            LocalDate today = LocalDate.now();
            LocalDateTime expectedStartDate = LocalDateTime.of(today.minusMonths(6), LocalTime.MIDNIGHT);
            assertThat(startDateCaptor.getValue()).isEqualTo(expectedStartDate);
        }

        @Test
        @DisplayName("period가 ONE_YEAR이면 12개월 전 자정이 startDate로 전달된다")
        void passesOneYearAgoStartDateWhenPeriodIsOneYear() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, OrderPeriod.ONE_YEAR, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            LocalDate today = LocalDate.now();
            LocalDateTime expectedStartDate = LocalDateTime.of(today.minusMonths(12), LocalTime.MIDNIGHT);
            assertThat(startDateCaptor.getValue()).isEqualTo(expectedStartDate);
        }

        @Test
        @DisplayName("period가 ALL이면 startDate가 null로 전달된다")
        void passesNullStartDateWhenPeriodIsAll() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, OrderPeriod.ALL, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            assertThat(startDateCaptor.getValue()).isNull();
        }

        @Test
        @DisplayName("status가 null이면 빈 상태 리스트가 전달된다")
        void passesEmptyStatusListWhenStatusIsNull() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("status가 SHIPPING이면 배송 관련 상태들이 전달된다")
        void passesShippingRelatedStatusesWhenStatusIsShipping() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, OrderStatusFilter.SHIPPING, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).containsExactlyInAnyOrder(
                    OrderStatus.PREPARING,
                    OrderStatus.SHIPPING
            );
        }

        @Test
        @DisplayName("status가 DELIVERED이면 DELIVERED 상태가 전달된다")
        void passesDeliveredStatusWhenStatusIsDelivered() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, OrderStatusFilter.DELIVERED, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).containsExactly(OrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("status가 CONFIRMED이면 CONFIRMED 상태가 전달된다")
        void passesConfirmedStatusWhenStatusIsConfirmed() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, OrderStatusFilter.CONFIRMED, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).containsExactlyInAnyOrder(
                    OrderStatus.PARTIALLY_CONFIRMED,
                    OrderStatus.CONFIRMED
            );
        }

        @Test
        @DisplayName("status가 CANCEL_RETURN이면 취소/반품 관련 상태들이 전달된다")
        void passesCancelReturnStatusesWhenStatusIsCancelReturn() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, OrderStatusFilter.CANCEL_RETURN, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).containsExactlyInAnyOrder(
                    OrderStatus.CANCELLED,
                    OrderStatus.RETURN_REQUESTED,
                    OrderStatus.RETURN_IN_PROGRESS,
                    OrderStatus.RETURN_INSPECTING,
                    OrderStatus.PARTIALLY_RETURNED,
                    OrderStatus.RETURNED,
                    OrderStatus.RETURN_REJECTED,
                    OrderStatus.RETURN_RESHIPPING_REQUESTED
            );
        }
    }

    // ==================================================================================
    // 포트 호출 검증
    // ==================================================================================

    @Nested
    @DisplayName("포트 호출 검증")
    class PortInvocationTest {

        @Test
        @DisplayName("findOrderPort.findByBuyerId를 정확히 한 번 호출한다")
        void callsFindOrderPortExactlyOnce() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort, times(1)).findByBuyerId(
                    eq(buyerId), any(), any(), anyList()
            );
        }

        @Test
        @DisplayName("주문이 존재하면 findProductByPricePolicyPort.findByPricePolicyIds를 한 번 호출한다")
        void callsFindProductByPricePolicyPortWhenOrdersExist() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            getOrderService.getBuyerOrderHistory(query);

            verify(findProductByPricePolicyPort, times(1)).findByPricePolicyIds(anyList());
        }

        @Test
        @DisplayName("주문이 없으면 findProductByPricePolicyPort를 호출하지 않는다")
        void doesNotCallFindProductByPricePolicyPortWhenNoOrders() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("findOrderProductPort와 상호작용하지 않는다")
        void doesNotInteractWithFindOrderProductPort() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            getOrderService.getBuyerOrderHistory(query);

            verifyNoInteractions(findOrderProductPort);
        }

        @Test
        @DisplayName("주문 상품의 pricePolicyId 중복이면 distinct하여 호출한다")
        void deduplicatesPricePolicyIdsBeforeCallingPort() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L, 200L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L, 300L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C")
                    ));

            getOrderService.getBuyerOrderHistory(query);

            verify(findProductByPricePolicyPort).findByPricePolicyIds(pricePolicyIdsCaptor.capture());
            assertThat(pricePolicyIdsCaptor.getValue()).hasSize(3);
            assertThat(pricePolicyIdsCaptor.getValue()).containsExactlyInAnyOrder(100L, 200L, 300L);
        }

        @Test
        @DisplayName("null인 pricePolicyId는 제외하고 호출한다")
        void excludesNullPricePolicyIdsBeforeCallingPort() {
            Long buyerId = 1L;
            Order order = createOrderWithMixedPricePolicyProducts(1L, buyerId, List.of(100L), 2);
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            getOrderService.getBuyerOrderHistory(query);

            verify(findProductByPricePolicyPort).findByPricePolicyIds(pricePolicyIdsCaptor.capture());
            assertThat(pricePolicyIdsCaptor.getValue()).doesNotContainNull();
            assertThat(pricePolicyIdsCaptor.getValue()).containsExactly(100L);
        }

        @Test
        @DisplayName("모든 pricePolicyId가 null이면 findProductByPricePolicyPort를 호출하지 않는다")
        void doesNotCallProductPortWhenAllPricePolicyIdsAreNull() {
            Long buyerId = 1L;
            Order order = createOrderWithNullPricePolicyProducts(1L, buyerId, 3);
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            getOrderService.getBuyerOrderHistory(query);

            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("주문 상품이 빈 목록이면 findProductByPricePolicyPort를 호출하지 않는다")
        void doesNotCallProductPortWhenOrderProductsAreEmpty() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of());
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            getOrderService.getBuyerOrderHistory(query);

            verifyNoInteractions(findProductByPricePolicyPort);
        }
    }

    // ==================================================================================
    // 엣지 케이스
    // ==================================================================================

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCaseTest {

        @Test
        @DisplayName("주문 상품이 빈 목록인 주문도 정상 반환된다")
        void returnsOrderWithEmptyProductList() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of());
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(1);
            assertThat(result.orders().get(0).getOrderProducts()).isEmpty();
        }

        @Test
        @DisplayName("findProductByPricePolicyPort가 null을 반환하면 빈 맵으로 처리한다")
        void handlesNullReturnFromProductPort() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(null);

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(1);
            assertThat(result.orderedProducts()).isEmpty();
        }

        @Test
        @DisplayName("단일 주문에 다수의 상품이 있는 경우 모든 pricePolicyId로 조회한다")
        void queriesAllPricePolicyIdsFromSingleOrderWithManyProducts() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L, 200L, 300L, 400L, 500L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C"),
                            400L, createProductInfo(400L, "상품D"),
                            500L, createProductInfo(500L, "상품E")
                    ));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            verify(findProductByPricePolicyPort).findByPricePolicyIds(pricePolicyIdsCaptor.capture());
            assertThat(pricePolicyIdsCaptor.getValue()).hasSize(5);
            assertThat(result.orderedProducts()).hasSize(5);
        }

        @Test
        @DisplayName("여러 주문에 동일한 pricePolicyId가 있으면 중복 제거하여 조회한다")
        void deduplicatesPricePolicyIdsAcrossMultipleOrders() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L, 200L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(100L, 300L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C")
                    ));

            getOrderService.getBuyerOrderHistory(query);

            verify(findProductByPricePolicyPort).findByPricePolicyIds(pricePolicyIdsCaptor.capture());
            assertThat(pricePolicyIdsCaptor.getValue())
                    .hasSize(3)
                    .containsExactlyInAnyOrder(100L, 200L, 300L);
        }

        @Test
        @DisplayName("상품명 필터로 모든 주문이 제거되면 빈 결과를 반환한다")
        void returnsEmptyResultWhenAllOrdersFilteredByProductName() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "존재하지않는상품");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B")
                    ));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).isEmpty();
            assertThat(result.orderedProducts()).isEmpty();
        }

        @Test
        @DisplayName("주문에 여러 상품 중 하나라도 상품명과 일치하면 해당 주문을 반환한다")
        void returnsOrderWhenAnyProductMatchesName() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L, 200L, 300L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품B");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C")
                    ));

            GetBuyerOrdersResult result = getOrderService.getBuyerOrderHistory(query);

            assertThat(result.orders()).hasSize(1);
            assertThat(result.orders().get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("status가 ALL이면 빈 상태 리스트가 전달된다")
        void passesEmptyStatusListWhenStatusIsAll() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, OrderStatusFilter.ALL, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("buyerId가 다른 값이면 각각 올바른 buyerId로 호출한다")
        void passesCorrectBuyerIdForDifferentBuyers() {
            Long buyerId1 = 10L;
            Long buyerId2 = 20L;
            GetBuyerOrderHistoryQuery query1 = createQuery(buyerId1, null, null, null);
            GetBuyerOrderHistoryQuery query2 = createQuery(buyerId2, null, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderHistory(query1);
            getOrderService.getBuyerOrderHistory(query2);

            verify(findOrderPort).findByBuyerId(eq(10L), any(), any(), anyList());
            verify(findOrderPort).findByBuyerId(eq(20L), any(), any(), anyList());
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private GetBuyerOrderHistoryQuery createQuery(
            Long buyerId,
            OrderPeriod period,
            OrderStatusFilter status,
            String productName
    ) {
        return GetBuyerOrderHistoryQuery.of(buyerId, period, status, productName);
    }

    private Order createOrderWithProducts(Long orderId, Long buyerId, List<Long> pricePolicyIds) {
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
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithNullPricePolicyProducts(Long orderId, Long buyerId, int count) {
        List<OrderProductSnapshotState> productStates = java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(null)
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
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithMixedPricePolicyProducts(
            Long orderId, Long buyerId, List<Long> validPricePolicyIds, int nullPricePolicyCount
    ) {
        List<OrderProductSnapshotState> validStates = validPricePolicyIds.stream()
                .map(pricePolicyId -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAID)
                        .build())
                .toList();

        List<OrderProductSnapshotState> nullStates = java.util.stream.IntStream.range(0, nullPricePolicyCount)
                .mapToObj(i -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(null)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAID)
                        .build())
                .toList();

        List<OrderProductSnapshotState> allStates = new java.util.ArrayList<>(validStates);
        allStates.addAll(nullStates);

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(allStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private ProductInfoResult createProductInfo(Long id, String name) {
        return new ProductInfoResult(id, null, name, "브랜드", null, null, null, List.of());
    }
}
